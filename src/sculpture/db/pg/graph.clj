(ns sculpture.db.pg.graph
  (:require
    [clojure.string :as string]
    [hugsql.core :as hugsql]
    [sculpture.db.pg.config :refer [db-spec]]
    [sculpture.db.pg.mapper :refer [db->]]))

(hugsql/def-db-fns "sculpture/db/pg/sql/graph.sql")

(defn select []
  (->> (-graph-select-sculptures
         @db-spec)
       (map db->)))

(defn search
  [{:keys [query limit]}]
  (if (string/blank? query)
    (throw (ex-info "Query must not be nil or blank" {}))
    (->> (-graph-search-entities
           @db-spec
           {:query query
            :limit limit})
         (map db->))))

#_(graph-search "Canadian")
#_(search {:query "John"})
#_(search {:query "John"
           :limit 1})


