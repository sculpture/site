(ns sculpture.admin.views.sidebar.entity.partials.photo-mosaic
  (:require
    [bloom.commons.pages :as pages]
    [sculpture.admin.views.sidebar.entity.partials.photos :refer [image-view]]))

(defn photo-mosaic-view
  "Items is a list of {:link string :photo photo}"
  [items]
  [:div.mosaic {:style {:width "100%"
                        :overflow "hidden"}}
   (->> [(take 2 items)
         (take 4 (drop 2 items))]
        (map-indexed
          (fn [index row]
            (let [row-ratio (apply +
                              (->> row
                                   (map :photo)
                                   (map (fn [photo]
                                          (/ (:photo/width photo)
                                             (:photo/height photo))))))]
              (when (seq row)
                ^{:key index}
                [:div.row {:style {:width "100%"
                                   :position "relative"
                                   :padding-bottom (str (/ 100 row-ratio) "%")}}
                 [:div {:style {:position "absolute"
                                :height "100%"
                                :width "100%"}}
                  (for [{:keys [link photo]} row]
                    ^{:key (:photo/id photo)}
                    [:a {:href link
                         :style {:width (str (* 100 (/ (/ (:photo/width photo)
                                                          (:photo/height photo))
                                                       row-ratio)) "%")
                                 :height "100%"
                                 :display "inline-block"
                                 :vertical-align "top"}}
                     [image-view {:photo photo
                                  :size :medium}]])]])))))])

(defn sculpture-mosaic-view
  [sculptures]
  [photo-mosaic-view
   (->> sculptures
        (map (fn [sculpture]
               {:link (pages/path-for [:page/sculpture {:id (:sculpture/id sculpture)}])
                :photo (first (:sculpture/photos sculpture))})))])
