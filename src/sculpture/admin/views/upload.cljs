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
      [:div
       [:div
        (case @stage
          :waiting
          [:div
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
          [:div
           "Uploading..." @progress "%"
           [preview-image-view @data-url]]
          :processing
          [:div
           "Processing..."
           [preview-image-view @data-url]]
          :error
          [:div "Error"
           @error])]])))
