(ns sculpture.db.postgres
  (:require
   [hugsql.core :as hugsql]
   [hugsql.adapter.next-jdbc :as next-adapter]
   [hikari-cp.core :as hikari]
   [sculpture.db.pg.mapper] ;; to extend protocols
   [sculpture.config :refer [config]]))

(defonce datasource
  (delay
    (hugsql/set-adapter! (next-adapter/hugsql-adapter-next-jdbc))
    (hikari/make-datasource {:jdbc-url (str "jdbc:" (config :db-url))})))

(def ^:dynamic db-spec datasource)

(hugsql/def-db-fns "sculpture/db/pg/sql/init.sql")
(hugsql/def-db-fns "sculpture/db/pg/sql/drop.sql")

(defn init! []
  (-init! @db-spec))

(defn drop! []
  (-drop! @db-spec))

(hugsql/def-db-fns "sculpture/db/pg/sql/upsert.sql")

(defn remove-namespaces [entity]
  (->> entity
       (map (fn [[k v]]
              [(keyword (name k)) v]))
       (into {})))

#_(remove-namespaces {:foo/bar 123})

(defn upsert-sculpture! [sculpture]
  (-upsert-sculpture!
    @db-spec
   (-> sculpture
       remove-namespaces
       (assoc :location-lng (:longitude (:sculpture/location sculpture)))
       (assoc :location-lat (:latitude (:sculpture/location sculpture)))
       (assoc :location-precision (:precision (:sculpture/location sculpture)))
       (dissoc :location))))

(defn upsert-region! [region]
  (-upsert-region!
   @db-spec
   (-> region
       remove-namespaces)))

(hugsql/def-db-fns "sculpture/db/pg/sql/util.sql")

(defn simplify-geojson [geojson]
  (->> (-simplify-geojson
         @db-spec
         {:geojson geojson})
       :geojson))
