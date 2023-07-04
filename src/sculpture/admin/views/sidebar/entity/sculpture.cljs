(ns sculpture.admin.views.sidebar.entity.sculpture
  (:require
    [clojure.string :as string]
    [bloom.commons.pages :as pages]
    [sculpture.admin.state.api :refer [dispatch!]]
    [sculpture.admin.views.sidebar.entity :refer [entity-handler]]
    [sculpture.admin.views.sidebar.entity.partials.photo-mosaic :refer [photo-mosaic-view]]
    [sculpture.admin.views.sidebar.entity.partials.related :refer [related-view]]))

(defn sculpture-entity-view
  [sculpture]
  [:div.sculpture.entity

   [photo-mosaic-view (->> (:sculpture/photos sculpture)
                           (map (fn [photo]
                                  {:link (pages/path-for [:page/photo {:id (:photo/id photo)}])
                                   :photo photo})))]

   [:div.info
    [:h1 (:sculpture/title sculpture)]
    [:h2
     (into [:div.artists]
           (interpose
             [:span ", "]
             (for [artist (:sculpture/artists sculpture)]
               [:a {:href (pages/path-for [:page/artist {:id (:artist/id artist)}])} (:artist/name artist)])))

     [:div.year (:sculpture/date sculpture)]]]

   [:div.meta
    (when (seq (:sculpture/sculpture-tags sculpture))
      [:div.row.tags {:title "Tags"}
       [:div.tags
        (interpose ", "
                   (for [sculpture-tag (:sculpture/sculpture-tags sculpture)]
                     ^{:key (:sculpture-tag/id sculpture-tag)}
                     [:a.tag {:href (pages/path-for [:page/sculpture-tag {:id (:sculpture-tag/id sculpture-tag)}])}
                      (:sculpture-tag/name sculpture-tag)]))]])

    (when (seq (:sculpture/materials sculpture))
      [:div.row.materials {:title "Materials"}
       [related-view
        {:entity-type :material
         :label-key :material/name}
        (:sculpture/materials sculpture)]])

    (when (:sculpture/link-wikipedia sculpture)
      [:div.row.wikipedia {:title "Wikipedia Link"}
       [:a.link {:href (:sculpture/link-wikipedia sculpture)} "Wikipedia"]])

    (when (:sculpture/location sculpture)
      [:div.row.location {:title "Location"}
       [:a {:href "#"
            :on-click (fn [e]
                        (.preventDefault e)
                        (dispatch! [:state.mega-map/go-to! (:sculpture/location sculpture)]))}
        (-> (:sculpture/location sculpture)
            (select-keys [:longitude :latitude])
            vals
            (->>
              (map (fn [c] (/ (js/Math.round (* c 100000)) 100000)))
              (string/join ", " )))]])

    (when-let [city (:sculpture/city sculpture)]
      [:div.row.city {:title "City"}
       [:a {:href (pages/path-for [:page/city {:id (:city/id city)}])}
        (interpose ", " [(:city/city city) (:city/region city) (:city/country city)])]])

    (when (seq (:sculpture/regions sculpture))
      [:div.row.regions {:title "Regions"}
       (interpose ", "
                  (for [region (:sculpture/regions sculpture)]
                    ^{:key (:region/id region)}
                    [:a
                     {:href (pages/path-for [:page/region {:id (:region/id region)}])}
                     (:region/name region)]))])

    (when (seq (:sculpture/nearby-regions sculpture))
      [:div.row.nearby {:title "Nearby"}
       (interpose ", "
                  (for [region (:sculpture/nearby-regions sculpture)]
                    ^{:key (:region/id region)}
                    [:a
                     {:href (pages/path-for [:page/region {:id (:region/id region)}])}
                     (:region/name region)]))])

    (when (seq (:sculpture/commissioned-by sculpture))
      [:div.row.commission {:title "Commissioned By"}
       (:sculpture/commissioned-by sculpture)])

    (when (seq (:sculpture/note sculpture))
      [:div.row.note {:title "Note"}
       (:sculpture/note sculpture)])

    (when (seq (:sculpture/segments sculpture))
      [:div.row.segments {:title "Segments"}
       (interpose ", "
                  (for [segment (:sculpture/segments sculpture)]
                    ^{:key (:segment/id segment)}
                    [:a {:href (pages/path-for [:page/segment {:id (:segment/id segment)}])}
                     (:segment/name segment)]))])]])

(defmethod entity-handler :sculpture
  [_ sculpture-id]
  {:identifier {:sculpture/id sculpture-id}
   :pattern [:sculpture/id
             {:sculpture/photos
              [:photo/id
               :photo/width
               :photo/height
               :photo/colors]}
             :sculpture/title
             :sculpture/date
             :sculpture/link-wikipedia
             :sculpture/location
             :sculpture/commissioned-by
             :sculpture/note
             {:sculpture/city
              [:city/id
               :city/city
               :city/region
               :city/country]}
             {:sculpture/sculpture-tags
              [:sculpture-tag/id
               :sculpture-tag/name]}
             {:sculpture/materials
              [:material/id
               :material/name]}
             {:sculpture/regions
              [:region/id
               :region/name
               :region/area]}
             {:sculpture/nearby-regions
              [:region/id
               :region/name
               :region/area]}
             {:sculpture/artists
              [:artist/id
               :artist/name]}
             {:sculpture/segments
              [:segment/id
               :segment/name]}]
   :view sculpture-entity-view})
