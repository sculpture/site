(ns sculpture.admin.views.sidebar.entity.nationality
  (:require
    [sculpture.admin.views.sidebar.entity :refer [entity-handler]]
    [sculpture.admin.views.sidebar.entity.partials.list :refer [entity-list-view]]
    [sculpture.admin.views.sidebar.entity.partials.photo-mosaic :refer [sculpture-mosaic-view]]))

(defn nationality-entity-view
  [nationality]
  [:div.nationality.entity
   [sculpture-mosaic-view (->> nationality
                               :nationality/artists
                               (map :artist/sculptures)
                               (map first))]
   [:div.info
    [:h1 (:nationality/demonym nationality)]
    [:h2 "Nationality"]]
   [:div.related
    [:h2 "Artists"]
    [:div.artists
     [entity-list-view (:nationality/artists nationality)]]]])

(defmethod entity-handler :nationality
  [_ nationality-id]
  {:identifier {:nationality/id nationality-id}
   :pattern [:nationality/id
             :nationality/demonym
             {:nationality/artists
              [:artist/id
               :artist/name
               {:artist/sculptures ;; TODO {:limit 1}
                [:sculpture/id
                 {:sculpture/photos
                  [:photo/id
                   :photo/width
                   :photo/height
                   :photo/colors]}]}]}]
   :view nationality-entity-view})

