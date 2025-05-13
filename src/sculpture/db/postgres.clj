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

(declare -init!)
(hugsql/def-db-fns "sculpture/db/pg/sql/init.sql")

(declare -drop!)
(hugsql/def-db-fns "sculpture/db/pg/sql/drop.sql")

(defn init! []
  (-init! @db-spec))

(defn drop! []
  (-drop! @db-spec))

(declare -upsert-sculpture!
         -upsert-region!)
(hugsql/def-db-fns "sculpture/db/pg/sql/upsert.sql")

(defn upsert-sculpture!
  [{:sculpture/keys [id location]}]
  (-upsert-sculpture!
    @db-spec
   {:id id
    :location-lng (:longitude location)
    :location-lat (:latitude location)
    :location-precision (:precision location)}))

(defn upsert-region!
  [{:region/keys [id geojson]}]
  (-upsert-region!
   @db-spec
   {:id id
    :geojson geojson}))

(declare -simplify-geojson)
(hugsql/def-db-fns "sculpture/db/pg/sql/util.sql")

(defn simplify-geojson [geojson]
  (->> (-simplify-geojson
         @db-spec
         {:geojson geojson})
       :geojson))
