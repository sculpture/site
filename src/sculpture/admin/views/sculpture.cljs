(ns sculpture.admin.views.sculpture
  (:require
    [sculpture.admin.views.photos :refer [photo-view]]
    [sculpture.admin.views.entity :refer [entity-view]]
    [sculpture.admin.views.object :refer [object-view]]
    [re-frame.core :refer [subscribe dispatch]]))

(defn photos-view [sculpture-id]
  (let [photos (subscribe [:photos-for-sculpture sculpture-id])]
    (fn []
      [:div.photos
       (for [photo @photos]
         ^{:key (photo :id)}
         [photo-view photo :thumb false])])))

(defmethod entity-view "sculpture"
  [sculpture]
  [:div.sculpture
   [:h1 (sculpture :title)]
   [photos-view (sculpture :id)]

   [object-view sculpture]])
