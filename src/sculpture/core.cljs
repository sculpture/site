(ns sculpture.core
  (:require
    [reagent.core :as r]))

(enable-console-print!)

(defonce state (r/atom {:photos []}))

(defn photo-view [file]
  (let [data-url (r/atom nil)
        reader (when-not @data-url
                 (doto (js/FileReader.)
                   (aset "onload" (fn [e]
                                    (reset! data-url (.. e -target -result))))
                   (.readAsDataURL file)))]
    (fn []
      [:div
        (.-name file)
        (when @data-url
          [:img {:src @data-url
                 :height "100px"}])])))

(defn app-view []
  [:div
   [:input {:type      "file"
            :multiple  true
            :on-change (fn [e]
                         (let [file-list (.. e -target -files)
                               files (map (fn [n]
                                            (aget file-list n))
                                          (range (.-length file-list)))]
                           (swap! state assoc :photos files)))}]
   (let [photos (@state :photos)]
     (for [photo photos]
       ^{:key (.-name photo)}
       [photo-view photo]))])


(defn render []
  (r/render-component [app-view] (.. js/document (getElementById "app"))))

(defn init []
  (render))

(defn reload []
  (render))
