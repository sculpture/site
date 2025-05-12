(ns sculpture.db.search
  (:require
   [clojure.string :as string]
   [sculpture.schema.schema :as schema]
   [sculpture.db.datascript :as db.ds]))

(defn search
  [{:keys [query limit types] :or {limit ##Inf}}]
  (let [query (string/lower-case query)]
    (->> types
         (mapcat (fn [entity-type]
                   (let [search-attr (->> (schema/schema entity-type)
                                          (keep (fn [[k opts]]
                                                  (when (:schema.attr/index-text? opts)
                                                    k)))
                                          first ;; for now
                                          )]
                     (->> (db.ds/q [:find '?id '?title
                                    :in '$ '?query
                                    :where
                                    ['?e search-attr '?title]
                                    ['(clojure.string/lower-case ?title) '?title-lower]
                                    ['(clojure.string/includes? ?title-lower ?query)]
                                    ['?e (schema/id-key entity-type) '?id]]
                                   query)
                          ;; not making the query any faster
                          ;; but by limiting here, sending less downstream
                          (take limit)
                          (map (fn [[id title]]
                                 {:id id
                                  :title title
                                  :type entity-type
                                  :subtitle nil
                                  :photo-id nil})))))))))

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
#_(search {:query "canad"
           :types ["sculpture"]
           :limit 5})
#_(search {:query "elo"
           :types ["sculpture"]
           :limit 5})

;; these not working after moving out of pg:
#_(search {:query "connection" ;; stemming
           :types ["sculpture"]
           :limit 5})
#_(search {:query "canad monum"
           :types ["sculpture"]
           :limit 5})
#_(search {:query "canadian bear"
           :types ["sculpture"]
           :limit 5})
