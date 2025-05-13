(ns sculpture.db.datascript
  (:require
   [datascript.core :as ds]
   [sculpture.schema.schema :as schema]
   [sculpture.schema.util :as schema.util]))

(defn prep-for-datascript
  [entity]
  (->>
   ;; only keep attributes that will be stored in datascript
   (select-keys entity (->> (schema/schema (schema.util/entity-type entity))
                            (filter (fn [[_k v]]
                                      (= :db/datascript (:schema.attr/db v))))
                            (map key)))
   ;; remove nils
   (filter (fn [[_k v]] v))
   ;; wrap all ref uuids in [:entity/id uuid]
   ;;    {:sculpture/material-ids [1 2]}
   ;;  becomes:
   ;;    {:sculpture/material-ids [[:material/id 1] [:material/id 2]]}
   (map (fn [[k v]]
          [k
           (if-let [id-key (some-> (get-in
                                     schema/by-id
                                     [(namespace k)
                                      :entity/spec
                                      k
                                      :schema.attr/relation
                                      1])
                                   schema/id-key)]
             (cond
               (uuid? v)
               [id-key v]
               (and (vector? v) (uuid? (first v)))
               (mapv (fn [x] [id-key x]) v)
               :else
               (throw (ex-info "Whut?" {})))
             v)]))
   (into {})))

(defn schema []
  (->> schema/entities
       (mapcat (fn [entity]
                 (->> (:entity/spec entity)
                      (filter (fn [[_k v]]
                                (= :db/datascript (:schema.attr/db v))))
                      (map (fn [[k v]]
                             [k (->> {:db/valueType (if (:schema.attr/relation v)
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
                                      :db/unique (:schema.attr/unique v)
                                      :db/index (:schema.attr/unique v)}
                                     (filter val)
                                     (into {}))])))))
       (into {})))

#_(schema)
#_(:sculpture/artist-ids (schema))

(defonce conn
  (ds/create-conn (schema)))

(defn reinit! []
  (alter-var-root #'conn (constantly (ds/create-conn (schema)))))

(defn upsert-entity!
  [entity]
  (ds/transact! conn [(prep-for-datascript entity)]))

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

#_(q '[:find (pull ?m [*])
       :where
       [?m :material/name "painted bronze"]])

#_(q '[:find ?m
       :where
       [?m :material/id #uuid "20ec9050-85db-4ec6-8fef-806598bcee75"]])
