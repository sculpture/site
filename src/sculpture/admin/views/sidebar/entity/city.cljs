(ns sculpture.admin.views.sidebar.entity.city
  (:require
    [sculpture.admin.views.sidebar.entity :refer [entity-handler]]
    [sculpture.admin.views.sidebar.entity.partials.photo-mosaic :refer [photo-mosaic-view]]
    [sculpture.admin.views.sidebar.entity.partials.related-sculptures :refer [related-sculptures-view]]))

(defn city-entity-view
  [city]
  [:div.city.entity
   [photo-mosaic-view (->> (:city/sculptures city)
                           (map :sculpture/photos)
                           (map first))]

   [:div.info
    [:h1 (:city/city city) ", " (:city/region city) ", " (:city/country city)]
    [:h2 "City"]]

   [:div.related
    [:h2 "Sculptures"]
    [related-sculptures-view (:city/sculptures city)]]])

(defmethod entity-handler :city
  [_ city-id]
  {:identifier {:city/id city-id}
   :pattern [:city/id
             :city/city
             :city/region
             :city/country
             {:city/sculptures
              [:sculpture/id
               :sculpture/title
               {:sculpture/photos
                [:photo/id
                 :photo/width
                 :photo/height
                 :photo/colors]}
               {:sculpture/artists
                [:artist/name]}]}]
   :view city-entity-view})
