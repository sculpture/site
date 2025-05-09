(ns sculpture.db.postgres
  (:require
   [clojure.string :as string]
   [hugsql.core :as hugsql]
   [hugsql.adapter.next-jdbc :as next-adapter]
   [hikari-cp.core :as hikari]
   [sculpture.config :refer [config]]
   [sculpture.schema.schema :as schema]
   [sculpture.db.pg.mapper :refer [->db]]))

(defonce datasource
  (delay
    (hugsql/set-adapter! (next-adapter/hugsql-adapter-next-jdbc))
    (hikari/make-datasource {:jdbc-url (str "jdbc:" (config :db-url))})))

(def ^:dynamic db-spec datasource)

(hugsql/def-db-fns "sculpture/db/pg/sql/init.sql")
(hugsql/def-db-fns "sculpture/db/pg/sql/views.sql")
(hugsql/def-db-fns "sculpture/db/pg/sql/drop.sql")

(defn reset-views! []
  (-views! @db-spec))

(defn init! []
  (-init! @db-spec)
  (reset-views!))

(defn drop! []
  (-drop! @db-spec))

(hugsql/def-db-fns "sculpture/db/pg/sql/upsert.sql")

(defn upsert-sculpture! [sculpture]
  (-upsert-sculpture!
    @db-spec
    (->db sculpture)))

(defn upsert-region! [region]
  (-upsert-region!
    @db-spec
    (->db region)))

(hugsql/def-db-fns "sculpture/db/pg/sql/util.sql")

(defn simplify-geojson [geojson]
  (->> (-simplify-geojson
         @db-spec
         {:geojson geojson})
       :geojson))

(hugsql/def-db-fns "sculpture/db/pg/sql/select.sql")

(def entity-type->db-table
  (zipmap (map :entity/id schema/entities)
          (map :entity/table schema/entities)))

(defn entity-counts []
  (-entity-counts @db-spec))

(defn search
  [{:keys [query limit types]}]
  (if (string/blank? query)
    (throw (ex-info "Query must not be nil or blank" {}))
    (->> (-search
           @db-spec
           ;; given input "a canadian monument"
           {:query query
            ;; will be: "*a canadian monument*
            :ilike-query query
            ;; will be: "a* & canadian* & monument*"
            :raw-tsquery (-> query
                             (string/split #" ")
                             (->>
                               (map (fn [s]
                                      (str s ":*")))
                               (string/join "&")))
            ;; will be: "canad* & monum*" (stemming stop words)
            :parsed-tsquery query
            :limit limit
            :types types})
         (map db->))))

#_(search {:query "Wood"
           :types ["sculpture" "artist"]})
#_(search {:query "John"
           :types ["artist"]
           :limit 5})
#_(search {:query "Can"
           :types ["nationality"]
           :limit 5})
#_(search {:query "end"
           :types ["sculpture"]
           :limit 5})
#_(search {:query "connection" ;; stemming
           :types ["sculpture"]
           :limit 5})
#_(search {:query "canad"
           :types ["sculpture"]
           :limit 5})
#_(search {:query "canad monum"
           :types ["sculpture"]
           :limit 5})
#_(search {:query "canadian bear"
           :types ["sculpture"]
           :limit 5})
#_(search {:query "elo"
           :types ["sculpture"]
           :limit 5})

; misc

(defn select-random-sculpture-slug []
  (->> (-select-random-sculpture-slug @db-spec)
       :slug))

#_(select-random-sculpture-slug)
