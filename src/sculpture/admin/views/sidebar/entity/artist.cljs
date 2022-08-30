(ns sculpture.admin.views.sidebar.entity.artist
 (:require
   [sculpture.admin.views.sidebar.entity :refer [entity-handler]]
   [sculpture.admin.views.sidebar.entity.partials.related-sculptures :refer [related-sculptures-view]]
   [sculpture.admin.views.sidebar.entity.partials.related :refer [related-view]]
   [sculpture.admin.views.sidebar.entity.partials.photo-mosaic :refer [photo-mosaic-view]]))

(defn artist-entity-view
  [artist]
  [:div.artist.entity
   [photo-mosaic-view (->> (:artist/sculptures artist)
                           (mapcat :sculpture/photos))]
   [:div.info
    [:h1 (:artist/name artist)]
    (when (:artist/birth-date artist)
      [:h2 [:div.year
            (:artist/birth-date artist)
            (when (:artist/death-date artist)
              (str "–" (:artist/death-date artist)))]])]
   [:div.meta
    (when (:artist/link-wikipedia artist)
      [:div.row.wikipedia {:title "Wikipedia Link"}
       [:a.link {:href (:artist/link-wikipedia artist)} "Wikipedia"]])
    (when (:artist/link-website artist)
      [:div.row.website {:title "Website Link"}
       [:a.link {:href (:artist/link-website artist)} "Website"]])
    (when (seq (:artist/artist-tags artist))
      [:div.row.tags {:title "Tags"}
       [related-view {:entity-type :artist-tag
                      :label-key :artist-tag/name}
        (:artist/artist-tags artist)]])
    (when (:artist/gender artist)
      [:div.row.gender {:title "Gender"}
       (:artist/gender artist)])
    (when (:artist/nationalities artist)
      [:div.row.nationalities {:title "Nationality"}
       [related-view {:entity-type :nationality
                      :label-key :nationality/demonym}
        (:artist/nationalities artist)]])]
   [:div.related
    [:h2 "Sculptures"]
    [related-sculptures-view (:artist/sculptures artist)]]])

(defmethod entity-handler :artist
  [_ artist-id]
  {:identifier {:artist/id artist-id}
   :pattern [:artist/id
             :artist/name
             :artist/birth-date
             :artist/link-wikipedia
             :artist/link-website
             :artist/gender
             {:artist/artist-tags [:artist-tag/id
                                   :artist-tag/name]}
             {:artist/nationalities
              [:nationality/id
               :nationality/demonym]}
             {:artist/sculptures
              [:sculpture/id
               :sculpture/title
               {:sculpture/photos
                [:photo/id
                 :photo/width
                 :photo/height
                 :photo/colors]}
               {:sculpture/artists
                [:artist/name]}]}]
   :view artist-entity-view})

