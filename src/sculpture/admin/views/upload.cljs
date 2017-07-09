(ns sculpture.admin.views.upload
  (:require
    [reagent.core :as r]
    [sculpture.admin.state.core :refer [dispatch!]]
    [sculpture.admin.cdn :as cdn]))

(defn preview-image-view [src]
  (when src
    [:img {:src src
           :style {:max-height "100px"
                   :max-width "100px"}}]))

(defn upload-view []
  (let [state (r/atom :waiting)
        data-url (r/atom nil)
        progress (r/atom nil)
        stage (r/atom :waiting) ; :uploading :processing
        image-data (r/atom nil)
        error (r/atom nil)
        reader (doto (js/FileReader.)
                 (aset "onload" (fn [e]
                                  (reset! data-url (.. e -target -result)))))
        on-progress (fn [{:keys [loaded total]}]
                      (reset! progress (/ loaded total))
                      (if (< @progress 1)
                        (reset! stage :uploading)
                        (reset! stage :processing)))
        on-success (fn [response]
                     (dispatch! [:sculpture.edit/create-entity response]))
        on-error (fn [e]
                   (reset! stage :error)
                   (reset! error e))]
    (fn []
      [:div.upload
       [:div.header
        [:h1 "Upload Photo"]
        [:button.close {:on-click (fn [_]
                                    (dispatch! [:set-main-page nil]))}
         "Close"]]
       [:div.content
        (case @stage
          :waiting
          [:div.waiting
           [:label
            [:input {:type "file"
                     :accept "image/*"
                     :on-change (fn [e]
                                  (let [files (.. e -target -files)
                                        file (aget files 0)]
                                    (.readAsDataURL reader file)
                                    (dispatch! [:sculpture.photo/upload file
                                                {:on-progress on-progress
                                                 :on-success on-success
                                                 :on-error on-error}])))}]]]

          :uploading
          [:div.uploading
           [preview-image-view @data-url]
           [:div "Uploading... " (int (* 100 @progress)) "%"]
           [:div.progress
            [:div.bar {:style {:width (str (int (* 100 @progress)) "%")}}]]]

          :processing
          [:div.processing
           [preview-image-view @data-url]
           [:div "Processing..."]]

          :error
          [:div.error
           [:div "Error"]
           [:div @error]])]])))
