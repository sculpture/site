(ns sculpture.admin.views.entity.sculpture
  (:require
    [sculpture.admin.state.core :refer [subscribe]]
    [sculpture.admin.views.entity :refer [entity-view]]
    [sculpture.admin.views.entity.partials.photos :refer [photo-view]]
    [sculpture.admin.views.entity.partials.map :refer [map-view]]
    [sculpture.admin.views.entity.partials.related-tags :refer [related-tags-view]]
    [sculpture.admin.views.entity.partials.related-materials :refer [related-materials-view]]
    [sculpture.admin.views.object :refer [object-view]]))

(defn photos-view [sculpture-id]
  (let [photos (subscribe [:photos-for-sculpture sculpture-id])]
    [:div.photos
     (for [photo @photos]
       ^{:key (photo :id)}
       [photo-view photo :thumb false])]))

(defmethod entity-view "sculpture"
  [sculpture]
  [:div.sculpture
   [:h1 (sculpture :title)]
   [photos-view (sculpture :id)]
   [:div.year (sculpture :year)]
   [related-tags-view (sculpture :tag-ids)]
   [related-materials-view (sculpture :material-ids)]
   [:div.note (sculpture :note)]

   (when (sculpture :commissioned-by)
     [:div.commissioned-by
      "Commissioned by "
      (sculpture :commissioned-by)])

   [map-view (sculpture :location)]])
