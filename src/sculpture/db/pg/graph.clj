(ns sculpture.db.pg.graph
  (:require
    [clojure.set :as set]
    [next.jdbc.result-set :as rs]
    [com.wsscode.pathom3.connect.indexes :as pci]
    [com.wsscode.pathom3.interface.smart-map :as psm]
    [com.wsscode.pathom3.interface.eql :as peql]
    [com.wsscode.pathom3.connect.operation :as pco]
    [next.jdbc :as jdbc]
    [honey.sql :as sql]
    [sculpture.db.pg.config :refer [db-spec]]))

(defn execute! [query]
  (jdbc/execute! @db-spec
                 query
                 {:builder-fn next.jdbc.result-set/as-kebab-maps}))

(def table-columns
  {:artist [:artist/id
            :artist/name
            :artist/slug
            :artist/gender
            :artist/link-website
            :artist/link-wikipedia
            :artist/bio
            :artist/birth-date
            :artist/death-date]
   :category [:category/id
              :category/slug
              :category/name]
   :city [:city/id
          :city/city
          :city/region
          :city/country
          :city/slug]
   :material [:material/id
              :material/name
              :material/slug]
   :photo [:photo/id
           :photo/captured-at
           :photo/user-id
           :photo/colors
           :photo/width
           :photo/height
           :photo/sculpture-id]
   :region [:region/id
            :region/name
            :region/slug
            :region/geojson
            :region/area
            :region/points-count]
   :sculpture [:sculpture/id
               :sculpture/title
               :sculpture/slug
               :sculpture/size
               :sculpture/note
               :sculpture/date
               :sculpture/commissioned-by
               :sculpture/location
               :sculpture/location-precision
               :sculpture/link-wikipedia
               :sculpture/city-id]})


;; artist

(pco/defresolver artists []
  {::pco/input []
   ::pco/output [{:artists (table-columns :artist)}]}
  {:artists
   (execute! (sql/format {:select :*
                          :from :artist}))})

#_(artists)

(pco/defresolver artist-by-id [{:artist/keys [id]}]
  {::pco/input [:artist/id]
   ::pco/output (table-columns :artist)}
  (first (execute! (sql/format {:select :*
                                :from :artist
                                :where [:= :artist/id id]}))))

#_(artist-by-id {:artist/id #uuid "f01c816e-53e3-4023-85a5-21c300a9b6b3"})

(pco/defresolver artist-artist-tags [{:artist/keys [id]}]
  {::pco/input [:artist/id]
   ::pco/output [{:artist/artist-tags [:artist-tag/id]}
                 :artist/artist-tag-ids]}
  (let [ids (->> (execute! (sql/format {:select [[[:raw "\"artist-tag-id\""] :artist-tag-id]]
                                        :from [:artists-artist-tags]
                                        :where [:= [:raw "\"artist-id\""] id]}))
                 (map (fn [r]
                        {:artist-tag/id (:artists-artist-tags/artist-tag-id r)})))]
  {:artist/artist-tags ids
   :artist/artist-tag-ids (map :artist-tag/id ids)}))

#_(artist-artist-tags {:artist/id #uuid "139ea168-bb33-4f0f-bd8f-24e25bee704c"})

(pco/defresolver artist-nationalities [{:artist/keys [id]}]
  {::pco/input [:artist/id]
   ::pco/output [{:artist/nationalities [:nationality/id]}
                 :artist/nationality-ids]}
  (let [ids (->> (execute! (sql/format {:select [[[:raw "\"nationality-id\""] :nationality-id]]
                                        :from [:artists-nationalities]
                                        :where [:= [:raw "\"artist-id\""] id]}))
                 (map (fn [r]
                        {:nationality/id (:artists-nationalities/nationality-id r)})))]
    {:artist/nationalities ids
     :artist/nationality-ids (map :nationality/id ids)}))

#_(artist-nationalities {:artist/id #uuid "f01c816e-53e3-4023-85a5-21c300a9b6b3"})

(pco/defresolver artist-sculptures [{:artist/keys [id]}]
  {::pco/input [:artist/id]
   ::pco/output [{:artist/sculptures [:sculpture/id]}
                 :artist/sculpture-ids]}
  (let [ids (->> (execute! (sql/format {:select [[[:raw "\"sculpture-id\""] :sculpture-id]]
                                        :from [:artists-sculptures]
                                        :where [:= [:raw "\"artist-id\""] id]}))
                 (map (fn [r]
                        {:sculpture/id (:artists-sculptures/sculpture-id r)})))]
  {:artist/sculptures ids
   :artist/sculpture-ids (map :sculpture/id ids)}))

#_(artist-sculptures {:artist/id #uuid "f01c816e-53e3-4023-85a5-21c300a9b6b3"})

;; artist-tag

(pco/defresolver artist-tag-by-id [{:artist-tag/keys [id]}]
  {::pco/input [:artist-tag/id]
   ::pco/output [:artist-tag/id
                 :artist-tag/name
                 :artist-tag/slug]}
  (first (execute! (sql/format {:select :*
                                :from :artist-tag
                                :where [:= :artist-tag/id id]}))))

#_(artist-tag-by-id {:artist-tag/id #uuid "af620c73-e4a8-460f-aec9-cec3014e9689"})

;; category

(pco/defresolver categories []
  {::pco/input []
   ::pco/output [{:categories (table-columns :category)}]}
  {:categories
   (execute! (sql/format {:select :*
                          :from :category}))})

#_(categories)

(pco/defresolver category-by-id [{:category/keys [id]}]
  {::pco/input [:category/id]
   ::pco/output (table-columns :category)}
  (first (execute! (sql/format {:select :*
                                :from :category
                                :where [:= :category/id id]}))))

#_(category-by-id {:category/id #uuid "2108c78a-fad9-4f34-8326-e748c3d03c05"})

;; city

(pco/defresolver cities []
  {::pco/input []
   ::pco/output [{:cities (table-columns :city)}]}
  {:cities
   (execute! (sql/format {:select :*
                          :from :city}))})

#_(cities)

(pco/defresolver city-by-id [{:city/keys [id]}]
  {::pco/input [:city/id]
   ::pco/output (table-columns :city)}
  (first (execute! (sql/format {:select [:*]
                                :from [:city]
                                :where [:= :id id]
                                :limit 1}))))

#_(city-by-id {:city/id #uuid "2a8b5ff3-81c9-4adf-8220-9d60723176e1"})

(pco/defresolver city-sculptures [{:city/keys [id]}]
  {::pco/input [:city/id]
   ::pco/output [{:city/sculptures [:sculpture/id]}
                 :city/sculpture-ids]}
  (let [ids (execute! (sql/format {:select :id
                                   :from :sculpture
                                   :where [:= [:raw "\"city-id\""] id]}))]
  {:city/sculptures ids
   :city/sculpture-ids (map :sculpture/id ids)}))

#_(city-sculptures {:city/id #uuid "2a8b5ff3-81c9-4adf-8220-9d60723176e1"})

;; material

(pco/defresolver materials []
  {::pco/input []
   ::pco/output [{:materials (table-columns :material)}]}
  {:materials
   (execute! (sql/format {:select :*
                          :from :material}))})

#_(materials)

(pco/defresolver material-by-id [{:material/keys [id]}]
  {::pco/input [:material/id]
   ::pco/output (table-columns :material)}
  (first (execute! (sql/format {:select :*
                                :from :material
                                :where [:= :material/id id]}))))

#_(material-by-id {:material/id #uuid "75dfd552-d16c-4149-9a5e-78848b93d168"})

(pco/defresolver material-sculptures [{:material/keys [id]}]
  {::pco/input [:material/id]
   ::pco/output [{:material/sculptures [:sculpture/id]}
                 :materia/sculpture-ids]}
  (let [ids (->> (execute! (sql/format {:select [[[:raw "\"sculpture-id\""] :sculpture-id]]
                                        :from [:materials-sculptures]
                                        :where [:= [:raw "\"material-id\""] id]}))
                 (map (fn [r]
                        {:sculpture/id (:materials-sculptures/sculpture-id r)})))]
    {:material/sculptures ids
     :material/sculpture-ids (map :sculpture/id ids)}))

#_(material-sculptures {:material/id #uuid "75dfd552-d16c-4149-9a5e-78848b93d168"})

;; nationality

(pco/defresolver nationality-by-id [{:nationality/keys [id]}]
  {::pco/input [:nationality/id]
   ::pco/output [:nationality/id
                 :nationality/nation
                 :nationality/demonym
                 :nationality/slug]}
  (first (execute! (sql/format {:select :*
                                :from :nationality
                                :where [:= :nationality/id id]}))))

#_(nationality-by-id {:nationality/id #uuid "9e5ed9ce-cb55-4441-b1c2-5da71d7f3baa"})

;; photo

(pco/defresolver photos []
  {::pco/input []
   ::pco/output [{:photos (table-columns :photo)}]}
  {:photos (execute! (sql/format {:select :*
                                  :from :photo}))})

#_(photos)

(pco/defresolver photo-by-id [{:photo/keys [id]}]
  {::pco/input [:photo/id]
   ::pco/output (table-columns :photo)}
  (first (execute! (sql/format {:select :*
                                :from :photo
                                :where [:= :photo/id id]}))))

#_(photo-by-id {:photo/id #uuid "153b622b-3c43-4474-987b-6997913684df"})

(pco/defresolver photo-user [{:photo/keys [user-id]}]
  {::pco/input [:photo/user-id]
   ::pco/output [{:photo/user [:user/id]}]}
  {:photo/user {:user/id user-id}})

(pco/defresolver photo-sculpture [{:photo/keys [sculpture-id]}]
  {::pco/input [:photo/sculpture-id]
   ::pco/output [{:photo/sculpture [:sculpture/id]}]}
  {:photo/sculpture {:sculpture/id sculpture-id}})

;; region

(defn fix-region [region]
  (-> region
      (set/rename-keys {:geojson :region/geojson
                        :area :region/area
                        :points-count :region/points-count})))

(pco/defresolver regions []
  {::pco/input []
   ::pco/output [{:regions (table-columns :region)}]}
  {:regions
   (->> (execute! (sql/format {:select [:*
                                        [[:ST_AsGeoJSON :region/shape]
                                         :geojson]
                                        [[:Round [:/ [:ST_Area :region/shape] 1000]] :area]
                                        [[:ST_NPoints [:cast :region/shape :geometry]] :points-count]]
                               :from :region}))
        (map fix-region))})

#_(regions)

(pco/defresolver region-by-id [{:region/keys [id]}]
  {::pco/input [:region/id]
   ::pco/output (table-columns :region)}
  (-> (first (execute! (sql/format {:select [:*
                                             [[:ST_AsGeoJSON :region/shape]
                                              :geojson]
                                             [[:Round [:/ [:ST_Area :region/shape] 1000]] :area]
                                             [[:ST_NPoints [:cast :region/shape :geometry]] :points-count]]
                                    :from :region
                                    :where [:= :id id]
                                    :limit 1})))
      fix-region))

#_(region-by-id {:region/id #uuid "bf27a5bb-93fe-4f6c-8fe7-db7b23ec8e39"})

(pco/defresolver region-region-tags [{:region/keys [id]}]
  {::pco/input [:region/id]
   ::pco/output [{:region/region-tags [:region-tag/id]}
                 :region/region-tag-ids]}
  (let [ids (->> (execute! (sql/format {:select [[[:raw "\"region-tag-id\""] :region-tag-id]]
                                        :from [:regions-region-tags]
                                        :where [:= [:raw "\"region-id\""] id]}))
                 (map (fn [r]
                        {:region-tag/id (:regions-region-tags/region-tag-id r)})))]
  {:region/region-tags ids
   :region/region-tag-ids (map :region-tag/id ids)}))

#_(region-region-tags {:region/id #uuid "bf27a5bb-93fe-4f6c-8fe7-db7b23ec8e39"})

(pco/defresolver region-sculptures [{:region/keys [id]}]
  {::pco/input [:region/id]
   ::pco/output [{:region/sculptures [:sculpture/id]}
                 :region/sculpture-ids]}
  (let [ids (execute! (sql/format {:select :sculpture/id
                                   :from :sculpture
                                   :where [:ST_DWithin
                                           {:select :region.shape
                                            :from :region
                                            :where [:= :region/id id]}
                                           :sculpture.location
                                           100]}))]
    {:region/sculptures ids
     :region/sculpture-ids (map :sculpture/id ids)}))

#_(region-sculptures {:region/id #uuid "bf27a5bb-93fe-4f6c-8fe7-db7b23ec8e39"})

;; region-tag

(pco/defresolver region-tag-by-id [{:region-tag/keys [id]}]
  {::pco/input [:region-tag/id]
   ::pco/output [:region-tag/id
                 :region-tag/name
                 :region-tag/slug]}
  (first (execute! (sql/format {:select :*
                                :from :region-tag
                                :where [:= :region-tag/id id]}))))

#_(region-tag-by-id {:region-tag/id #uuid "c6980378-c006-49d9-a681-effc4d36aea9"})

;; sculpture

(defn fix-sculpture [s]
  (-> s
      (assoc-in [:sculpture/location :precision]
        (s :sculpture/location-precision))
      (dissoc :sculpture/location-precision)))

(pco/defresolver sculptures []
  {::pco/input []
   ::pco/output [{:sculptures (table-columns :sculpture)}]}
  {:sculptures
   (map fix-sculpture (execute! (sql/format {:select :*
                                             :from :sculpture})))})

#_(sculptures)

(pco/defresolver sculpture-by-id [{:sculpture/keys [id]}]
  {::pco/input [:sculpture/id]
   ::pco/output (table-columns :sculpture)}
  (fix-sculpture (first (execute! (sql/format {:select :*
                                               :from :sculpture
                                               :where [:= :sculpture/id id]})))))

#_(sculpture-by-id {:sculpture/id #uuid "f6687354-9e7c-4cb2-a644-14e1cf96fc34"})

(pco/defresolver sculpture-artists [{:sculpture/keys [id]}]
  {::pco/input [:sculpture/id]
   ::pco/output [{:sculpture/artists [:artist/id]}
                 :sculpture/artist-ids]}
  (let [ids (->> (execute! (sql/format {:select [[[:raw "\"artist-id\""] :artist-id]]
                                        :from :artists-sculptures
                                        :where [:= [:raw "\"sculpture-id\""] id]}))
                 (map (fn [r]
                        (set/rename-keys r {:artists-sculptures/artist-id :artist/id}))))]
    {:sculpture/artists ids
     :sculpture/artist-ids (map :artist/id ids)}))

#_(sculpture-artists {:sculpture/id #uuid "f6687354-9e7c-4cb2-a644-14e1cf96fc34"})

(pco/defresolver sculpture-city [{:sculpture/keys [city-id]}]
  {::pco/input [:sculpture/city-id]
   ::pco/output [{:sculpture/city [:city/id]}]}
  {:sculpture/city {:city/id city-id}})

#_(sculpture-city {:sculpture/city-id #uuid "2a8b5ff3-81c9-4adf-8220-9d60723176e1"})

(pco/defresolver sculpture-materials [{:sculpture/keys [id]}]
  {::pco/input [:sculpture/id]
   ::pco/output [{:sculpture/materials [:material/id]}
                 :sculpture/material-ids]}
  (let [ids (->> (execute! (sql/format {:select [[[:raw "\"material-id\""] :material-id]]
                                        :from :materials-sculptures
                                        :where [:= [:raw "\"sculpture-id\""] id]}))
                 (map (fn [r]
                        (set/rename-keys r {:materials-sculptures/material-id :material/id}))))]
    {:sculpture/materials ids
     :sculpture/material-ids (map :material/id ids)}))

#_(sculpture-materials {:sculpture/id #uuid "f6687354-9e7c-4cb2-a644-14e1cf96fc34"})

(pco/defresolver sculpture-nearby-regions [{:sculpture/keys [location]}]
  {::pco/input [:sculpture/location]
   ::pco/output [{:sculpture/nearby-regions [:region/id]}
                 :sculpture/nearby-region-ids]}
  (let [ids (execute! (sql/format {:select [:id
                                            [[:ST_Distance
                                              :region/shape
                                              [:ST_MakePoint
                                               (:longitude location)
                                               (:latitude location)]] :distance]]
                                   :from :region
                                   :where [:and
                                           [:ST_DWithin
                                            :region/shape
                                            [:ST_MakePoint
                                             (:longitude location)
                                             (:latitude location)]
                                            100]
                                           [:>
                                            [:ST_Distance
                                             :region/shape
                                             [:ST_MakePoint
                                              (:longitude location)
                                              (:latitude location)]]
                                            0]]
                                   :order-by [:distance]}))]
  {:sculpture/nearby-regions ids
   :sculpture/nearby-region-ids (map :region/id ids)}))

#_(sculpture-nearby-regions {:sculpture/location {:latitude 43.66576
                                                  :longitude -79.38785}})

(pco/defresolver sculpture-photos [{:sculpture/keys [id]}]
  {::pco/input [:sculpture/id]
   ::pco/output [{:sculpture/photos
                  [:photo/id
                   :photo/captured-at
                   :photo/user-id
                   :photo/colors
                   :photo/width
                   :photo/height
                   :photo/sculpture-id]}
                 :sculpture/photo-ids]}
  (let [photos (execute! (sql/format {:select :*
                                      :from :photo
                                      :where [:= [:raw "\"sculpture-id\""] id]}))]
    {:sculpture/photos photos
     :sculpture/photo-ids (map :photo/id photos)}))

#_(sculpture-photos {:sculpture/id #uuid "01b5a5b6-b797-42e6-adc5-f283686e6b44"})

(pco/defresolver sculpture-regions [{:sculpture/keys [location]}]
  {::pco/input [:sculpture/location]
   ::pco/output [{:sculpture/regions [:region/id]}
                 :sculpture/region-ids]}
  (let [ids (execute! (sql/format {:select [:id
                                            [[:ST_Area [:ST_Envelope [:cast :region/shape :geometry]]] :size]]
                                   :from :region
                                   :where [:ST_Covers
                                           :region/shape
                                           [:ST_MakePoint
                                            (:longitude location)
                                            (:latitude location)]]
                                   :order-by [:size]}))]
    {:sculpture/regions ids
     :sculpture/region-ids (map :region/id ids)}))

#_(sculpture-regions {:sculpture/location {:latitude 43.6442
                                           :longitude -79.38771}})

(pco/defresolver sculpture-sculpture-tags [{:sculpture/keys [id]}]
  {::pco/input [:sculpture/id]
   ::pco/output [{:sculpture/sculpture-tags [:sculpture-tag/id]}
                 :sculpture/sculpture-tag-ids]}
  (let [ids (->> (execute! ["SELECT \"sculpture-tag-id\" FROM \"sculptures_sculpture-tags\"
                            WHERE \"sculpture-id\" = ?" id])
                 (map (fn [r]
                        (set/rename-keys r {:sculptures-sculpture-tags/sculpture-tag-id :sculpture-tag/id}))))]
    {:sculpture/sculpture-tags ids
     :sculpture/sculpture-tag-ids (map :sculpture-tag/id ids)}))

#_(sculpture-sculpture-tags {:sculpture/id #uuid "01b5a5b6-b797-42e6-adc5-f283686e6b44"})

;; sculpture-tag

(pco/defresolver sculpture-tag-by-id [{:sculpture-tag/keys [id]}]
  {::pco/input [:sculpture-tag/id]
   ::pco/output [:sculpture-tag/id
                 :sculpture-tag/name
                 :sculpture-tag/slug
                 :sculpture-tag/category-id
                 {:sculpture-tag/category [:category/id]}]}
  (let [e (first (execute! (sql/format {:select :*
                                        :from :sculpture-tag
                                        :where [:= :sculpture-tag/id id]
                                        :limit 1})))]
    (assoc e
      :sculpture-tag/category
      {:category/id (:sculpture-tag/category-id e)})))

#_(sculpture-tag-by-id {:sculpture-tag/id #uuid "521f00f2-f976-4b69-89cf-6e8db0c94f29"})

(pco/defresolver sculpture-tag-sculptures [{:sculpture-tag/keys [id]}]
  {::pco/input [:sculpture-tag/id]
   ::pco/output [{:sculpture-tag/sculptures [:sculpture/id]}
                 :sculpture-tag/sculpture-ids]}
  (let [ids (->> (execute! (sql/format {:select [[[:raw "\"sculpture-id\""] :sculpture-id]]
                                        :from [:sculptures-sculpture-tags]
                                        :where [:= [:raw "\"sculpture-tag-id\""] id]}))
                 (map (fn [r]
                        {:sculpture/id (:sculptures-sculpture-tags/sculpture-id r)})))]
    {:sculpture-tag/sculptures ids
     :sculpture-tag/sculpture-ids (map :sculpture/id ids)}))

#_(sculpture-tag-sculptures {:sculpture-tag/id #uuid "521f00f2-f976-4b69-89cf-6e8db0c94f29"})

;; user

(pco/defresolver user-by-id [{:user/keys [id]}]
  {::pco/input [:user/id]
   ::pco/output [:user/id
                 :user/email
                 :user/name
                 :user/avatar]}
  (let [p (first (execute! (sql/format {:select :*
                                        :from :users
                                        :where [:= :users/id id]})))]
    (set/rename-keys p {:users/id :user/id
                        :users/email :user/email
                        :users/avatar :user/avatar
                        :users/name :user/name})))

#_(user-by-id {:user/id #uuid "013ec717-531b-4b30-bacf-8a07f33b0d43"})

(def indexes
  (pci/register [artists
                 artist-by-id
                 artist-artist-tags
                 artist-nationalities
                 artist-sculptures

                 ;; artist-tags
                 artist-tag-by-id

                 categories
                 category-by-id

                 cities
                 city-by-id
                 city-sculptures

                 materials
                 material-by-id
                 material-sculptures

                 ;; nationalities
                 nationality-by-id
                 ;; nationality-artists

                 photos
                 photo-by-id
                 photo-user
                 photo-sculpture

                 regions
                 region-by-id
                 region-region-tags
                 region-sculptures

                 ;; region-tags
                 region-tag-by-id
                 ;; region-tag-regions

                 sculptures
                 sculpture-by-id
                 sculpture-artists
                 sculpture-city
                 sculpture-materials
                 sculpture-nearby-regions
                 sculpture-photos
                 sculpture-regions
                 sculpture-sculpture-tags

                 ;; sculpture-tags
                 sculpture-tag-by-id
                 sculpture-tag-sculptures

                 ;; users
                 user-by-id
                 ;; user-photos
                 ]))

;; smart-map-interface
#_(:sculpture/date (psm/smart-map indexes
                                  {:sculpture/id #uuid "f6687354-9e7c-4cb2-a644-14e1cf96fc34"}))

;; single eql query with identifier
#_(peql/process indexes
                [{[:sculpture/id #uuid "f6687354-9e7c-4cb2-a644-14e1cf96fc34"]
                          [:sculpture/date]}])

;; two part eql identifuer and equery
#_(peql/process indexes
                {:sculpture/id #uuid "f6687354-9e7c-4cb2-a644-14e1cf96fc34"}
                [:sculpture/date])

(defn query [query-id-or-identifier pattern]
  (cond
    (map? query-id-or-identifier)
    (peql/process indexes query-id-or-identifier pattern)
    (keyword? query-id-or-identifier)
    (peql/process indexes [{query-id-or-identifier pattern}])))


#_(query :regions [:region/name])

#_(query {:sculpture/id #uuid "0ef9c6f1-a415-45b2-9afd-925c00ff7955"}
         [:sculpture/location])

#_(query {:region/id #uuid "34338123-76c8-4e73-ac74-1855dd3f87ce"}
         [:region/shape])

#_(query {:artist/id #uuid "f01c816e-53e3-4023-85a5-21c300a9b6b3"}
         [:artist/artist-tag-ids])
