(ns sculpture.admin.views.sidebar.entity.artist-tag
  (:require
    [sculpture.admin.views.sidebar.entity :refer [entity-handler]]
    [sculpture.admin.views.sidebar.entity.partials.list :refer [entity-list-view]]))

(defn artist-tag-entity-view
  [artist-tag]
  [:div.artist-tag.entity
   [:div.info
    [:h1 (:artist-tag/name artist-tag)]]
   [:div.related
    [:h2 "Artists"]
    [:div.artists
     [entity-list-view (:artist-tag/artists artist-tag)]]]])

(defmethod entity-handler :artist-tag
  [_ artist-tag-id]
  {:identifier {:artist-tag/id artist-tag-id}
   :pattern [:artist-tag/id
             :artist-tag/name
             {:artist-tag/artists
              [:artist/id
               :artist/name
               {:artist/sculptures
                ;; TODO {:limit 1}
                [:sculpture/id
                 {:sculpture/photos
                  [:photo/id
                   :photo/width
                   :photo/height
                   :photo/colors]}]}]}]
   :view artist-tag-entity-view})
