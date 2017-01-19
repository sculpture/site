(ns sculpture.admin.views.entity.partials.photo-mosaic
  (:require
    [sculpture.admin.routes :as routes]
    [sculpture.admin.views.entity.partials.photos :refer [image-view]]))

(defn photo-view [photo]
  [:a {:href (routes/entity-path {:id (photo :id)})}
   [image-view {:photo photo
                :size :large}]])

(defn photo-mosaic-view [photos]
   (if (= 1 (count photos))
     [:div.photo-mosaic
      [:div.single
       [photo-view (first photos)]]]
     [:div.photo-mosaic
      [:div.many
       [:div.col
        (for [photo (take 3 photos)]
          ^{:key (photo :id)}
          [photo-view photo])]
       [:div.col
        (for [photo (take 3 (drop 3 photos))]
          ^{:key (photo :id)}
          [photo-view photo])]]]))

