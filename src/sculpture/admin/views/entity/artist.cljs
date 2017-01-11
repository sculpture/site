(ns sculpture.admin.views.entity.artist
 (:require
   [re-frame.core :refer [subscribe]]
   [sculpture.admin.views.entity :refer [entity-view]]
   [sculpture.admin.views.entity.partials.related-sculptures :refer [related-sculptures-view]]
   [sculpture.admin.views.object :refer [object-view]]))

(defmethod entity-view "artist"
  [artist]
  (let [sculptures (subscribe [:sculptures-for-artist (artist :id)])]
    [:div.artist
     [:h1 (artist :name)]
     [related-sculptures-view @sculptures]
     [object-view artist]]))


