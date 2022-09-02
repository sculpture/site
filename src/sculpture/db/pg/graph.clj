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
   :artist-tag [:artist-tag/id
                :artist-tag/name
                :artist-tag/slug]
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
   :nationality [:nationality/id
                 :nationality/nation
                 :nationality/demonym
                 :nationality/slug]
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
            :region/geojson ]
   :region-tag [:region-tag/id
                :region-tag/name
                :region-tag/slug]
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
               :sculpture/city-id]
   :sculpture-tag [:sculpture-tag/id
                   :sculpture-tag/name
                   :sculpture-tag/slug
                   :sculpture-tag/category-id]
   :user [:user/id
          :user/email
          :user/name
          :user/avatar]})

(defn fix-sculpture [sculpture]
  (-> sculpture
      (assoc-in [:sculpture/location :precision]
        (sculpture :sculpture/location-precision))
      (dissoc :sculpture/location-precision)))

(defn fix-region [region]
  (-> region
      (set/rename-keys {:geojson :region/geojson
                        :area :region/area
                        :points-count :region/points-count})))

(defn fix-user [user]
  ;; postgres doesn't allow a :user table
  (set/rename-keys user {:users/id :user/id
                         :users/email :user/email
                         :users/avatar :user/avatar
                         :users/name :user/name}))

;; artist

(pco/defresolver artists [env _]
  {::pco/input []
   ::pco/output [{:artists (table-columns :artist)}]}
  (let [gender (:gender (pco/params env))]
    {:artists
     (execute! (sql/format {:select :*
                            :from :artist
                            :where (cond
                                     gender
                                     [:= :artist/gender gender]
                                     :else
                                     [])}))}))

#_(artists)

#_(peql/process (pci/register artists)
                '[(:artists {:gender "female"})])

(pco/defresolver artist-by-id [{:artist/keys [id]}]
  {::pco/input [:artist/id]
   ::pco/output (table-columns :artist)}
  (first (execute! (sql/format {:select :*
                                :from :artist
                                :where [:= :artist/id id]}))))

#_(artist-by-id {:artist/id #uuid "f01c816e-53e3-4023-85a5-21c300a9b6b3"})

(pco/defresolver artist-by-slug [{:artist/keys [slug]}]
  {::pco/input [:artist/slug]
   ::pco/output (table-columns :artist)}
  (first (execute! (sql/format {:select :*
                                :from :artist
                                :where [:= :artist/slug slug]}))))

#_(artist-by-slug {:artist/slug "kosso-eloul"})

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
                 :artist/sculpture-ids
                 :artist/sculpture-count]}
  (let [ids (->> (execute! (sql/format {:select [[[:raw "\"sculpture-id\""] :sculpture-id]]
                                        :from [:artists-sculptures]
                                        :where [:= [:raw "\"artist-id\""] id]}))
                 (map (fn [r]
                        {:sculpture/id (:artists-sculptures/sculpture-id r)})))]
  {:artist/sculptures ids
   :artist/sculpture-ids (map :sculpture/id ids)
   :artist/sculpture-count (count ids)}))

#_(artist-sculptures {:artist/id #uuid "f01c816e-53e3-4023-85a5-21c300a9b6b3"})

;; artist-tag

(pco/defresolver artist-tags []
  {::pco/input []
   ::pco/output [{:artist-tags (table-columns :artist-tag)}]}
  {:artist-tags
   (execute! (sql/format {:select :*
                          :from :artist-tag}))})

#_(artist-tags)

(pco/defresolver artist-tag-by-id [{:artist-tag/keys [id]}]
  {::pco/input [:artist-tag/id]
   ::pco/output (table-columns :artist-tag)}
  (first (execute! (sql/format {:select :*
                                :from :artist-tag
                                :where [:= :artist-tag/id id]}))))

#_(artist-tag-by-id {:artist-tag/id #uuid "af620c73-e4a8-460f-aec9-cec3014e9689"})

(pco/defresolver artist-tag-by-slug [{:artist-tag/keys [slug]}]
  {::pco/input [:artist-tag/slug]
   ::pco/output (table-columns :artist-tag)}
  (first (execute! (sql/format {:select :*
                                :from :artist-tag
                                :where [:= :artist-tag/slug slug]}))))

#_(artist-tag-by-slug {:artist-tag/slug "famous"})

(pco/defresolver artist-tag-artists [{:artist-tag/keys [id]}]
  {::pco/input [:artist-tag/id]
   ::pco/output [{:artist-tag/artists [:artist/id]}]}
  (let [ids (->> (execute! (sql/format {:select [[[:raw "\"artist-id\""] :artist-id]]
                                        :from [:artists-artist-tags]
                                        :where [:= [:raw "\"artist-tag-id\""] id]}))
                 (map (fn [r]
                        {:artist/id (:artists-artist-tags/artist-id r)})))]
    {:artist-tag/artists ids}))


#_(artist-tag-artists {:artist-tag/id #uuid "af620c73-e4a8-460f-aec9-cec3014e9689"})

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

(pco/defresolver category-by-slug [{:category/keys [slug]}]
  {::pco/input [:category/slug]
   ::pco/output (table-columns :category)}
  (first (execute! (sql/format {:select :*
                                :from :category
                                :where [:= :category/slug slug]}))))

#_(category-by-slug {:category/slug "sculpture-form"})

(pco/defresolver category-sculpture-tags [{:category/keys [id]}]
  {::pco/input [:category/id]
   ::pco/output [{:category/sculpture-tags (table-columns :sculpture-tag)}]}
  {:category/sculpture-tags
   (execute! (sql/format {:select :*
                          :from :sculpture-tag
                          :where [:= [:raw "\"category-id\""] id]}))})

#_(category-sculpture-tags {:category/id #uuid "2108c78a-fad9-4f34-8326-e748c3d03c05"})

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
  (first (execute! (sql/format {:select :*
                                :from :city
                                :where [:= :id id]
                                :limit 1}))))

#_(city-by-id {:city/id #uuid "2a8b5ff3-81c9-4adf-8220-9d60723176e1"})

(pco/defresolver city-by-slug [{:city/keys [slug]}]
  {::pco/input [:city/slug]
   ::pco/output (table-columns :city)}
  (first (execute! (sql/format {:select :*
                                :from :city
                                :where [:= :slug slug]
                                :limit 1}))))

#_(city-by-slug {:city/slug "warsaw-poland"})

(pco/defresolver city-sculptures [{:city/keys [id]}]
  {::pco/input [:city/id]
   ::pco/output [{:city/sculptures (table-columns :sculpture)}
                 :city/sculpture-ids]}
  (let [sculptures (execute! (sql/format {:select :*
                                          :from :sculpture
                                          :where [:= [:raw "\"city-id\""] id]}))]
    {:city/sculptures (map fix-sculpture sculptures)
     :city/sculpture-ids (map :sculpture/id sculptures)}))

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

(pco/defresolver material-by-slug [{:material/keys [slug]}]
  {::pco/input [:material/slug]
   ::pco/output (table-columns :material)}
  (first (execute! (sql/format {:select :*
                                :from :material
                                :where [:= :material/slug slug]}))))

#_(material-by-slug {:material/slug "bronze"})

(pco/defresolver material-sculptures [{:material/keys [id]}]
  {::pco/input [:material/id]
   ::pco/output [{:material/sculptures [:sculpture/id]}
                 :material/sculpture-ids
                 :material/sculpture-count]}
  (let [ids (->> (execute! (sql/format {:select [[[:raw "\"sculpture-id\""] :sculpture-id]]
                                        :from [:materials-sculptures]
                                        :where [:= [:raw "\"material-id\""] id]}))
                 (map (fn [r]
                        {:sculpture/id (:materials-sculptures/sculpture-id r)})))]
    {:material/sculptures ids
     :material/sculpture-ids (map :sculpture/id ids)
     :material/sculpture-count (count ids)}))

#_(material-sculptures {:material/id #uuid "75dfd552-d16c-4149-9a5e-78848b93d168"})

;; nationality

(pco/defresolver nationalities []
  {::pco/input []
   ::pco/output [{:nationalities (table-columns :nationality)}]}
  {:nationalities (execute! (sql/format {:select :*
                                         :from :nationality}))})

#_(nationalities)

(pco/defresolver nationality-by-id [{:nationality/keys [id]}]
  {::pco/input [:nationality/id]
   ::pco/output (table-columns :nationality)}
  (first (execute! (sql/format {:select :*
                                :from :nationality
                                :where [:= :nationality/id id]}))))

#_(nationality-by-id {:nationality/id #uuid "9e5ed9ce-cb55-4441-b1c2-5da71d7f3baa"})

(pco/defresolver nationality-by-slug [{:nationality/keys [slug]}]
  {::pco/input [:nationality/slug]
   ::pco/output (table-columns :nationality)}
  (first (execute! (sql/format {:select :*
                                :from :nationality
                                :where [:= :nationality/slug slug]}))))

#_(nationality-by-slug {:nationality/slug "canadian"})

(pco/defresolver nationality-artists [{:nationality/keys [id]}]
  {::pco/input [:nationality/id]
   ::pco/output [{:nationality/artists [:artist/id]}
                 :nationality/artist-ids]}
  (let [ids (->> (execute! (sql/format {:select [[[:raw "\"artist-id\""] :artist-id]]
                                        :from [:artists-nationalities]
                                        :where [:= [:raw "\"nationality-id\""] id]}))
                 (map (fn [r]
                        {:artist/id (:artists-nationalities/artist-id r)})))]
    {:nationality/artists ids
     :nationality/artist-ids (map :artist/id ids)}))

#_(nationality-artists {:nationality/id #uuid "9e5ed9ce-cb55-4441-b1c2-5da71d7f3baa"})

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



(pco/defresolver regions []
  {::pco/input []
   ::pco/output [{:regions (into (table-columns :region)
                                 [:region/area
                                  :region/points-count])}]}
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
   ::pco/output (into (table-columns :region)
                             [:region/area
                              :region/points-count])}
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

(pco/defresolver region-by-slug [{:region/keys [slug]}]
  {::pco/input [:region/slug]
   ::pco/output (into (table-columns :region)
                      [:region/area
                       :region/points-count])}
  (-> (first (execute! (sql/format {:select [:*
                                             [[:ST_AsGeoJSON :region/shape]
                                              :geojson]
                                             [[:Round [:/ [:ST_Area :region/shape] 1000]] :area]
                                             [[:ST_NPoints [:cast :region/shape :geometry]] :points-count]]
                                    :from :region
                                    :where [:= :slug slug]
                                    :limit 1})))
      fix-region))

#_(region-by-slug {:region/slug "toronto"})

(pco/defresolver region-parent-region [{:region/keys [id]}]
  {::pco/input [:region/id]
   ::pco/output [{:region/parent-region (table-columns :region)}]}
  {:region/parent-region (first (execute! (sql/format {:select :*
                                                       :from :region
                                                       :left-join [[:region :parent]
                                                                   [:= :parent/id
                                                                    {:select :ancestor/id
                                                                     :from [[:region :ancestor]]
                                                                     :where [:and
                                                                             [:!= :region/id :ancestor/id]
                                                                             [:raw "region.shape::geometry @ ancestor.shape::geometry" ]
                                                                             [:ST_CoveredBy
                                                                              [:ST_Centroid [:cast :region/shape :geometry]]
                                                                              [:cast :ancestor/shape :geometry]]]
                                                                     :order-by [[[:ST_Area [:cast :ancestor/shape :geometry]]]]
                                                                     :limit 1}]]
                                                       :where [:= :region/id id]})))})

#_(region-parent-region {:region/id #uuid "bf27a5bb-93fe-4f6c-8fe7-db7b23ec8e39"})

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
                 :region/sculpture-ids
                 :region/sculpture-count]}
  (let [sculptures (execute! (sql/format {:select :*
                                          :from :sculpture
                                          :where [:ST_DWithin
                                                  {:select :region.shape
                                                   :from :region
                                                   :where [:= :region/id id]}
                                                  :sculpture.location
                                                  100]}))]
    {:region/sculptures (map fix-sculpture sculptures)
     :region/sculpture-ids (map :sculpture/id sculptures)
     :region/sculpture-count (count sculptures)}))

#_(region-sculptures {:region/id #uuid "bf27a5bb-93fe-4f6c-8fe7-db7b23ec8e39"})

;; region-tag

(pco/defresolver region-tags []
  {::pco/input []
   ::pco/output [{:region-tags (table-columns :region-tag)}]}
  {:region-tags
   (execute! (sql/format {:select :*
                          :from :region-tag}))})

#_(region-tags)

(pco/defresolver region-tag-by-id [{:region-tag/keys [id]}]
  {::pco/input [:region-tag/id]
   ::pco/output (table-columns :region-tag)}
  (first (execute! (sql/format {:select :*
                                :from :region-tag
                                :where [:= :region-tag/id id]}))))

#_(region-tag-by-id {:region-tag/id #uuid "c6980378-c006-49d9-a681-effc4d36aea9"})

(pco/defresolver region-tag-by-slug [{:region-tag/keys [slug]}]
  {::pco/input [:region-tag/slug]
   ::pco/output (table-columns :region-tag)}
  (first (execute! (sql/format {:select :*
                                :from :region-tag
                                :where [:= :region-tag/slug slug]}))))

#_(region-tag-by-slug {:region-tag/slug "city"})

(pco/defresolver region-tag-regions [{:region-tag/keys [id]}]
  {::pco/input [:region-tag/id]
   ::pco/output [{:region-tag/regions [:region/id]}
                 :region-tag/region-ids]}
  (let [ids (->> (execute! (sql/format {:select [[[:raw "\"region-id\""] :region-id]]
                                        :from :regions-region-tags
                                        :where [:= [:raw "\"region-tag-id\""] id]}))
                 (map (fn [r]
                        (set/rename-keys r {:regions-region-tags/region-id :region/id}))))]
    {:region-tag/regions ids
     :region-tag/region-ids (map :region/id ids)}))

#_(region-tag-regions {:region-tag/id #uuid "c6980378-c006-49d9-a681-effc4d36aea9"})

;; sculpture



(pco/defresolver sculptures [env _]
  {::pco/input []
   ::pco/output [{:sculptures (table-columns :sculpture)}]}
  {:sculptures
   (let [decade (:decade (pco/params env))]
     (->> (execute! (sql/format (cond
                                  decade
                                  (let [date-start (str (/ decade 10) "*")
                                        date-end (str (+ decade 9) "-12-31")]
                                    {:select :*
                                     :from :sculpture
                                     :where [:between :sculpture/date date-start date-end]})
                                  :else
                                  {:select :*
                                   :from :sculpture})))
          (map fix-sculpture)))})

#_(sculptures)

#_(peql/process (pci/register sculptures)
                '[(:sculptures {:decade 1960})])

(pco/defresolver sculpture-by-id [{:sculpture/keys [id]}]
  {::pco/input [:sculpture/id]
   ::pco/output (table-columns :sculpture)}
  (fix-sculpture (first (execute! (sql/format {:select :*
                                               :from :sculpture
                                               :where [:= :sculpture/id id]})))))

#_(sculpture-by-id {:sculpture/id #uuid "f6687354-9e7c-4cb2-a644-14e1cf96fc34"})

(pco/defresolver sculpture-by-slug [{:sculpture/keys [slug]}]
  {::pco/input [:sculpture/slug]
   ::pco/output (table-columns :sculpture)}
  (fix-sculpture (first (execute! (sql/format {:select :*
                                               :from :sculpture
                                               :where [:= :sculpture/slug slug]})))))

#_(sculpture-by-slug {:sculpture/slug "braha"})

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
   ::pco/output [{:sculpture/nearby-regions (table-columns :region)}
                 :sculpture/nearby-region-ids]}
  (let [regions (execute! (sql/format {:select :*
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
                                       :order-by [[[:ST_Distance
                                                    :region/shape
                                                    [:ST_MakePoint
                                                     (:longitude location)
                                                     (:latitude location)]]]]}))]
  {:sculpture/nearby-regions (map fix-region regions)
   :sculpture/nearby-region-ids (map :region/id regions)}))

#_(sculpture-nearby-regions {:sculpture/location {:latitude 43.66576
                                                  :longitude -79.38785}})

(pco/defresolver sculpture-photos [{:sculpture/keys [id]}]
  {::pco/input [:sculpture/id]
   ::pco/output [{:sculpture/photos (table-columns :photo)}
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
  (let [regions (execute! (sql/format {:select [:*
                                                [[:ST_Area [:ST_Envelope [:cast :region/shape :geometry]]] :size]]
                                       :from :region
                                       :where [:ST_Covers
                                               :region/shape
                                               [:ST_MakePoint
                                                (:longitude location)
                                                (:latitude location)]]
                                       :order-by [:size]}))]
    {:sculpture/regions (map fix-region regions)
     :sculpture/region-ids (map :region/id regions)}))

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

(pco/defresolver sculpture-tags []
  {::pco/input []
   ::pco/output [{:sculpture-tags (table-columns :sculpture-tag)}]}
  {:sculpture-tags (execute! (sql/format {:select :*
                                          :from :sculpture-tag}))})

#_(sculpture-tags)

(pco/defresolver sculpture-tag-by-id [{:sculpture-tag/keys [id]}]
  {::pco/input [:sculpture-tag/id]
   ::pco/output (table-columns :sculpture-tag)}
  (first (execute! (sql/format {:select :*
                                :from :sculpture-tag
                                :where [:= :sculpture-tag/id id]
                                :limit 1}))))

#_(sculpture-tag-by-id {:sculpture-tag/id #uuid "521f00f2-f976-4b69-89cf-6e8db0c94f29"})

(pco/defresolver sculpture-tag-by-slug [{:sculpture-tag/keys [slug]}]
  {::pco/input [:sculpture-tag/slug]
   ::pco/output (table-columns :sculpture-tag)}
  (first (execute! (sql/format {:select :*
                                :from :sculpture-tag
                                :where [:= :sculpture-tag/slug slug]
                                :limit 1}))))

#_(sculpture-tag-by-slug {:sculpture-tag/slug "red"})

(pco/defresolver sculpture-tag-category [{:sculpture-tag/keys [category-id]}]
  {::pco/input [:sculpture-tag/category-id]
   ::pco/output [{:sculpture-tag/category [:category/id]}]}
  {:sculpture-tag/category {:category/id category-id}})

(pco/defresolver sculpture-tag-sculptures [{:sculpture-tag/keys [id]}]
  {::pco/input [:sculpture-tag/id]
   ::pco/output [{:sculpture-tag/sculptures [:sculpture/id]}
                 :sculpture-tag/sculpture-ids
                 :sculpture-tag/sculpture-count]}
  (let [ids (->> (execute! (sql/format {:select [[[:raw "\"sculpture-id\""] :sculpture-id]]
                                        :from [:sculptures-sculpture-tags]
                                        :where [:= [:raw "\"sculpture-tag-id\""] id]}))
                 (map (fn [r]
                        {:sculpture/id (:sculptures-sculpture-tags/sculpture-id r)})))]
    {:sculpture-tag/sculptures ids
     :sculpture-tag/sculpture-ids (map :sculpture/id ids)
     :sculpture-tag/sculpture-count (count ids)}))

#_(sculpture-tag-sculptures {:sculpture-tag/id #uuid "521f00f2-f976-4b69-89cf-6e8db0c94f29"})

;; user

(pco/defresolver users []
  {::pco/input []
   ::pco/output [{:users (table-columns :user)}]}
  {:users (->> (execute! (sql/format {:select :*
                                      :from :users}))
               (map fix-user))})

#_(users)

(pco/defresolver user-by-id [{:user/keys [id]}]
  {::pco/input [:user/id]
   ::pco/output (table-columns :user)}
  (fix-user (first (execute! (sql/format {:select :*
                                          :from :users
                                          :where [:= :users/id id]})))))

#_(user-by-id {:user/id #uuid "013ec717-531b-4b30-bacf-8a07f33b0d43"})

(pco/defresolver user-photos [{:user/keys [id]}]
  {::pco/input [:user/id]
   ::pco/output [{:user/photos [:photo/id]}]}
  {:user/photos
   (execute! (sql/format {:select :*
                          :from :photo
                          :where [:= [:raw "\"user-id\""] id]}))})

#_(user-photos)

(def indexes
  (pci/register [artists
                 artist-by-id
                 artist-by-slug
                 artist-artist-tags
                 artist-nationalities
                 artist-sculptures

                 artist-tags
                 artist-tag-by-id
                 artist-tag-by-slug
                 artist-tag-artists

                 categories
                 category-by-id
                 category-by-slug
                 category-sculpture-tags

                 cities
                 city-by-id
                 city-by-slug
                 city-sculptures

                 materials
                 material-by-id
                 material-by-slug
                 material-sculptures

                 nationalities
                 nationality-by-id
                 nationality-by-slug
                 nationality-artists

                 photos
                 photo-by-id
                 photo-user
                 photo-sculpture

                 regions
                 region-by-id
                 region-by-slug
                 region-parent-region
                 region-region-tags
                 region-sculptures

                 region-tags
                 region-tag-by-id
                 region-tag-by-slug
                 region-tag-regions

                 sculptures
                 sculpture-by-id
                 sculpture-by-slug
                 sculpture-artists
                 sculpture-city
                 sculpture-materials
                 sculpture-nearby-regions
                 sculpture-photos
                 sculpture-regions
                 sculpture-sculpture-tags

                 sculpture-tags
                 sculpture-tag-by-id
                 sculpture-tag-by-slug
                 sculpture-tag-category
                 sculpture-tag-sculptures

                 users
                 user-by-id
                 user-photos
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
    (query-id-or-identifier (peql/process indexes [{query-id-or-identifier pattern}]))
    (nil? pattern)
    (peql/process indexes query-id-or-identifier)))

#_(query :regions [:region/name])

#_(query {:sculpture/id #uuid "0ef9c6f1-a415-45b2-9afd-925c00ff7955"}
         [:sculpture/location])

#_(query {:region/id #uuid "34338123-76c8-4e73-ac74-1855dd3f87ce"}
         [:region/shape])

#_(query {:artist/id #uuid "f01c816e-53e3-4023-85a5-21c300a9b6b3"}
         [:artist/artist-tag-ids])

#_(query '[{(:sculptures {:decade 1960}) [:sculpture/id]}] nil)

#_(do
    (require '[com.wsscode.pathom.viz.ws-connector.core :as pvc])
    (require [com.wsscode.pathom.viz.ws-connector.pathom3 :as p.connector])

    (let [env (p.connector/connect-env
                  (pci/register indexes)
                  {::pvc/parser-id :sculpture})]
        (peql/process env
                        {:photo/id #uuid "153b622b-3c43-4474-987b-6997913684df"}
                        [{:photo/user [{:user/photos [:photo/colors]}]}])))
