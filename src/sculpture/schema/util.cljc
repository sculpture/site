(ns sculpture.schema.util
  (:require
    [sculpture.schema.schema :as schema]))

(defn label [entity]
  (->> (vals schema/label-key)
       (map (fn [k]
              (entity k)))
       (remove nil?)
       first))

(defn entity-type [entity]
  (namespace (key (first entity))))

#_(entity-type {:sculpture/foo "asd"})

(defn entity-id [entity]
  (let [entity-type (entity-type entity)]
    (entity (keyword entity-type "id"))))

(defn entity-slug [entity]
  (let [entity-type (entity-type entity)]
    (entity (keyword entity-type "slug"))))

#_(entity-id {:sculpture/id "asd"})

#_(schema/schema "sculpture")

(defn entity-keys
  [entity-type]
  (keys (schema/schema entity-type)))

#_(entity-keys "sculpture")

(defn default-entity
  [entity-type]
  (->> (schema/schema entity-type)
       ;; mapcat + array-map preserves key order
       (mapcat (fn [[k v]]
                 [k (:default v)]))
       (apply array-map)))

#_(default-entity "sculpture")

(defn keys-of-all-entities []
  (->> schema/schema
       vals
       (mapcat keys)
       set))

#_(keys-of-all-entities)

;; schema, just keys, without types
(def schema-by-keys
  (->> schema/schema
       vals
       (apply merge)))

(defn attr->input [k]
  (get-in schema-by-keys [k :input]))

#_(attr->input :sculpture/title)
