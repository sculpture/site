(ns sculpture.db.pg.graph
  (:require
    [hugsql.core :as hugsql]
    [sculpture.db.pg.config :refer [db-spec]]
    [sculpture.db.pg.mapper :refer [db->]]))

(hugsql/def-db-fns "sculpture/db/pg/sql/graph.sql")

(defn select []
  (->> (-graph-select-sculptures
         @db-spec)
       (map db->)))

(defn search [query]
  (->> (-graph-search-entities
         @db-spec
         {:query query})
       (map db->)))

#_(graph-search "Canadian")
#_(search "Kosso")


