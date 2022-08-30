(ns sculpture.admin.views.sidebar.entity.partials.photo-mosaic
  (:require
    [bloom.commons.pages :as pages]
    [sculpture.admin.views.sidebar.entity.partials.photos :refer [image-view]]))

(defn photo-mosaic-view [photos]
  [:div.mosaic {:style {:width "100%"
                        :overflow "hidden"}}
   (map-indexed
     (fn [index row-photos]
       (let [row-ratio (apply +
                         (map (fn [photo]
                                (/ (:photo/width photo)
                                   (:photo/height photo)))
                              row-photos))]
         (when (seq row-photos)
           ^{:key index}
           [:div.row {:style {:width "100%"
                              :position "relative"
                              :padding-bottom (str (/ 100 row-ratio) "%")}}
            [:div {:style {:position "absolute"
                           :height "100%"
                           :width "100%"}}
             (for [photo row-photos]
               ^{:key (:photo/id photo)}
               [:a {:href (pages/path-for [:page/photo {:id (:photo/id photo)}])
                    :style {:width (str (* 100 (/ (/ (:photo/width photo)
                                                     (:photo/height photo))
                                                  row-ratio)) "%")
                            :height "100%"
                            :display "inline-block"
                            :vertical-align "top"}}
                [image-view {:photo photo
                             :size :medium}]])]])))
     [(take 2 photos)
      (take 4 (drop 2 photos))])])

