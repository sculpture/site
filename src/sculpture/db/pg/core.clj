(ns sculpture.db.pg.core
  (:require
    [hugsql.core :as hugsql]
    [sculpture.db.pg.config :refer [db-spec]]))

(hugsql/def-db-fns "sculpture/db/pg/sql/init.sql")
(hugsql/def-db-fns "sculpture/db/pg/sql/views.sql")
(hugsql/def-db-fns "sculpture/db/pg/sql/drop.sql")

(defn reset-views! []
  (-views! db-spec))

(defn init! []
  (-init! db-spec)
  (reset-views!))

(defn drop! []
  (-drop! db-spec))

