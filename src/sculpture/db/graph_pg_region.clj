(ns sculpture.db.graph-pg-region
  (:require
   [clojure.set :as set]
   [next.jdbc.result-set :as rs]
   [com.wsscode.pathom3.connect.operation :as pco]
   [next.jdbc :as jdbc]
   [honey.sql :as sql]
   [sculpture.db.postgres :refer [db-spec]]))

(defn execute! [query]
  (jdbc/execute! @db-spec
                 query
                 {:builder-fn next.jdbc.result-set/as-kebab-maps}))

;; region ---

(defn fix-region [region]
  (-> region
      (set/rename-keys {:geojson :region/geojson
                        :area :region/area
                        :points-count :region/points-count})))

(def base-region-query
  {:select [:region/id
            [[:ST_AsGeoJSON :region/shape]
             :geojson]
            [[:Round [:/ [:ST_Area :region/shape] 1000]] :area]
            [[:ST_NPoints [:cast :region/shape :geometry]] :points-count]]
   :from :region})

(def base-region-attrs
  [:region/id
   :region/area
   :region/points-count
   :region/geojson])

(pco/defresolver
 pg-regions
 []
 {::pco/input []
  ::pco/output [{:regions base-region-attrs}]}
 {:regions
  (->> (execute! (sql/format base-region-query))
       (map fix-region))})

#_(pg-regions)

(pco/defresolver
 pg-region-by-id
 [{:region/keys [id]}]
 {::pco/input [:region/id]
  ::pco/output base-region-attrs}
 (-> (first (execute! (sql/format (assoc base-region-query
                                    :where [:= :id id]
                                    :limit 1))))
     fix-region))

#_(pg-region-by-id {:region/id #uuid "bf27a5bb-93fe-4f6c-8fe7-db7b23ec8e39"})

(pco/defresolver
 pg-region-parent-region
 [{:region/keys [id]}]
 {::pco/input [:region/id]
  ::pco/output [{:region/parent-region [:region/id]}]}
 {:region/parent-region (first (execute! (sql/format {:select :parent/id
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

#_(pg-region-parent-region {:region/id #uuid "bf27a5bb-93fe-4f6c-8fe7-db7b23ec8e39"})

;; sculpture ---

(defn fix-sculpture [sculpture]
  (-> sculpture
      (assoc-in [:sculpture/location :precision]
        (sculpture :sculpture/location-precision))
      (dissoc :sculpture/location-precision)))

(pco/defresolver
 pg-region-sculptures
 [{:region/keys [id]}]
 {::pco/input [:region/id]
  ::pco/output [{:region/sculptures [:sculpture/id
                                     :sculpture/location]}
                :region/sculpture-ids
                :region/sculpture-count]}
 (let [sculptures (execute! (sql/format {:select [:id :location :location-precision]
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

#_(pg-region-sculptures {:region/id #uuid "bf27a5bb-93fe-4f6c-8fe7-db7b23ec8e39"})

(pco/defresolver
 sculpture-nearby-regions
 [{:sculpture/keys [location]}]
 {::pco/input [:sculpture/location]
  ::pco/output [{:sculpture/nearby-regions [:region/id]}
                :sculpture/nearby-region-ids]}
 (let [regions (execute! (sql/format {:select :id
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
   {:sculpture/nearby-regions regions
    :sculpture/nearby-region-ids (map :region/id regions)}))

#_(sculpture-nearby-regions {:sculpture/location {:latitude 43.66576
                                                  :longitude -79.38785}})

(pco/defresolver
 sculpture-regions
 [{:sculpture/keys [location]}]
 {::pco/input [:sculpture/location]
  ::pco/output [{:sculpture/regions [:region/id]}
                :sculpture/region-ids]}
 (let [regions (execute! (sql/format {:select [:id
                                               ;; for sorting
                                               [[:ST_Area [:ST_Envelope [:cast :region/shape :geometry]]] :size]]
                                      :from :region
                                      :where [:ST_Covers
                                              :region/shape
                                              [:ST_MakePoint
                                               (:longitude location)
                                               (:latitude location)]]
                                      :order-by [:size]}))]
   {:sculpture/regions (map (fn [region] (dissoc region :size)) regions)
    :sculpture/region-ids (map :region/id regions)}))

#_(sculpture-regions {:sculpture/location {:latitude 43.6442
                                           :longitude -79.38771}})

(pco/defresolver
 sculpture-location
 [{:sculpture/keys [id]}]
 {::pco/input [:sculpture/id]
  ::pco/output [:sculpture/location]}
 (fix-sculpture (first (execute! (sql/format {:select [:sculpture/location
                                                       :sculpture/location-precision]
                                              :from :sculpture
                                              :where [:= :sculpture/id id]
                                              :limit 1})))))

#_(sculpture-location {:sculpture/id #uuid "0ef9c6f1-a415-45b2-9afd-925c00ff7955"})

(def resolvers
  [pg-regions
   pg-region-by-id
   pg-region-parent-region
   pg-region-sculptures
   sculpture-location
   sculpture-nearby-regions
   sculpture-regions])
