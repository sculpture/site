(ns sculpture.db.datalog
  (:require
   [datalevin.core :as d]
   [datascript.core :as ds]
   [sculpture.schema.schema :as schema]))

(defn schema []
  (->> schema/entities
       (mapcat (fn [entity]
                 (->> (:entity/spec entity)
                      (map (fn [[k v]]
                             [k (->> {
                                      :db/valueType (if (:schema.attr/relation v)
                                                      :db.type/ref
                                                      (if (contains? v :schema.attr/type)
                                                        ;; datascript doesn't want types except refs and tuples
                                                        nil #_(:schema.attr/type v)
                                                        (println "type missing for: " k)))
                                      :db/cardinality (case (first (:schema.attr/relation v))
                                                        :one
                                                        :db.cardinality/one
                                                        :many
                                                        :db.cardinality/many
                                                        nil)
                                      :db/unique (when (:schema.attr/unique? v)
                                                   :db.unique/identity)}
                                     (filter val)
                                     (into {}))])))))
       (into {})))

#_(schema)
#_(:sculpture/artist-ids (schema))

#_(defonce conn
  (d/get-conn "./datalevin" (schema)))

(defonce conn
  (ds/create-conn (schema)))

(defn upsert-entity!
  [entity]
  (ds/transact! conn [entity]))

(defn q
  [query & args]
  (apply ds/q query @conn args))

#_(q '[:find ?t
       :where
       [?m :material/name "steel"]
       [?s :sculpture/material-ids ?m]
       [?s :sculpture/title ?t]])

#_(q '[:find ?n
       :where
       [?t :artist-tag/slug "famous"]
       [?a :artist/artist-tag-ids ?t]
       [?a :artist/id ?n]
       #_[_ :artist/artist-tags ?t]
       #_[?s :sculpture/title ?t]])
