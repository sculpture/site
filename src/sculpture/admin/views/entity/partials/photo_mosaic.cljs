(ns sculpture.admin.views.entity.partials.photo-mosaic
  (:require
    [sculpture.admin.routes :as routes]
    [sculpture.admin.views.entity.partials.photos :refer [photo-view]]))

(defn photo-mosaic-view [photos]
  [:div.photo-mosaic
   (for [photo photos]
     ^{:key (photo :id)}
     [:a {:href (routes/entity-path {:id (photo :id)})}
      [photo-view {:photo photo
                   :size :large}]])])

