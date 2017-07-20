(ns sculpture.db.pg.core
  (:require
    [hugsql.core :as hugsql]
    [sculpture.db.pg.config :refer [db-spec]]))

(hugsql/def-db-fns "sculpture/db/pg/sql/init.sql")
(hugsql/def-db-fns "sculpture/db/pg/sql/drop.sql")

(defn init! []
  (-init! db-spec))

(defn drop! []
  (-drop! db-spec))

