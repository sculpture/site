(ns sculpture.db.pg.util
  (:require
    [hugsql.core :as hugsql]
    [sculpture.db.pg.config :refer [db-spec]]))

(hugsql/def-db-fns "sculpture/db/pg/sql/util.sql")

(defn simplify-geojson [geojson]
  (->> (-simplify-geojson
         db-spec
         {:geojson geojson})
       :geojson))
