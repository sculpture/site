(ns sculpture.admin.views.sidebar.entity.material
  (:require
    [sculpture.admin.views.sidebar.entity :refer [entity-handler]]
    [sculpture.admin.views.sidebar.entity.partials.photo-mosaic :refer [photo-mosaic-view]]
    [sculpture.admin.views.sidebar.entity.partials.related-sculptures :refer [related-sculptures-view]]))

(defn material-entity-view
  [material]
  [:div.material.entity
   [photo-mosaic-view (->> (:material/sculptures material)
                           (map :sculpture/photos)
                           (map first))]
   [:div.info
    [:h1 (:material/name material)]
    [:h2 "Material"]]

   [:div.related
    [:h2 "Sculptures"]
    [related-sculptures-view (:material/sculptures material)]]])

(defmethod entity-handler :material
  [_ material-id]
  {:identifier {:material/id material-id}
   :pattern [:material/id
             :material/name
             {:material/sculptures
              [:sculpture/id
               :sculpture/title
               {:sculpture/photos
                [:photo/id
                 :photo/width
                 :photo/height
                 :photo/colors]}
               {:sculpture/artists
                [:artist/name]}]}]
   :view material-entity-view})
