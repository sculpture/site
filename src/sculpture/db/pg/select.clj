(ns sculpture.db.pg.select
  (:require
   [clojure.string :as string]
   [hugsql.core :as hugsql]
   [sculpture.schema.schema :as schema]
   [sculpture.db.pg.config :refer [db-spec]]
   [sculpture.db.pg.mapper :refer [db->]]))

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
