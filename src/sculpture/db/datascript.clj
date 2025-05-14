(ns sculpture.db.datascript
  (:require
   [clojure.set :as set]
   [datascript.core :as ds]
   [sculpture.schema.schema :as schema]
   [sculpture.schema.util :as schema.util]))

(defn schema []
  (->> schema/entities
       (mapcat (fn [entity]
                 (->> (:entity/spec entity)
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

(defn delta-transactions
  [entity]
  ;; datascript doesn't allow nils
  ;; so we need to figure out which things were removed
  ;; and retract them explicitly
  (let [entity-type (schema.util/entity-type entity)
        pull-pattern (->> (schema/by-id entity-type)
                          :entity/spec
                          (mapv (fn [[k v]]
                                  (if-let [[_cardinality other-entity-type] (:schema.attr/relation v)]
                                    {k [(schema/id-key other-entity-type)]}
                                    k))))
        id-key (schema/id-key entity-type)
        before-entity (->> (q [:find (list 'pull '?e pull-pattern) '.
                               :in '$ '?value
                               :where
                               ['?e id-key '?value]]
                              (id-key entity))
                           ;; convert refs in the form [{:entity/id uuid}] to lookup refs [[:entity/id uuid]]
                           (map (fn [[k v]]
                                  [k
                                   (if-let [[cardinality _other-entity-type]
                                            (get-in schema/by-id [entity-type :entity/spec k :schema.attr/relation])]
                                     (case cardinality
                                       :one
                                       (first v)
                                       :many
                                       (mapv first v))
                                     v)]))
                           (into {}))
        after-entity (->> entity
                          (filter (fn [[k v]] v))
                          ;; wrap all ref uuids in [:entity/id uuid]
                          ;;    {:sculpture/material-ids [1 2]}
                          ;;  becomes:
                          ;;    {:sculpture/material-ids [[:material/id 1] [:material/id 2]]}
                          ;; (assuming we are getting uuids)
                          (map (fn [[k v]]
                                 [k
                                  (if-let [[cardinality other-entity-type]
                                           (get-in schema/by-id [entity-type :entity/spec k :schema.attr/relation])]
                                    (let [id-key (schema/id-key other-entity-type)]
                                      (case cardinality
                                        :one
                                        [id-key v]
                                        :many
                                        (mapv (fn [x] [id-key x]) v)))
                                    v)]))
                          (into {}))
        attrs (->> (schema/by-id entity-type)
                   :entity/spec
                   keys)
        lookup-ref (if (id-key before-entity)
                     [id-key (id-key entity)]
                     -1)]
    (->> attrs
         (mapcat (fn [attr]
                 (let [before-value (attr before-entity)
                       after-value (attr after-entity)]
                   (cond
                     (= :many (get-in schema/by-id [entity-type :entity/spec attr :schema.attr/relation 0]))
                     (let [before-set (set before-value)
                           after-set (set after-value)
                           added (set/difference after-set before-set)
                           removed (set/difference before-set after-set)]
                       (concat
                        (map (fn [v] [:db/add lookup-ref attr v]) added)
                        (map (fn [v] [:db/retract lookup-ref attr v]) removed)))
                     (= before-value after-value)
                     []
                     (nil? (attr after-entity))
                     [[:db/retract lookup-ref attr (attr before-entity)]]
                     :else
                     [[:db/add lookup-ref attr (attr after-entity)]])))))))

(defn upsert-entity!
  [entity]
  (ds/transact! conn (delta-transactions entity)))
