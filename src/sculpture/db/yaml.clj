(ns sculpture.db.yaml
  (:require
    [yaml.writer]
    [yaml.reader]))

; Specification for YAML files:
;   - one folder per entity type
;   - file name is uuid of entity id
;   - every entity must have, at minimum:
;      - :id UUID
;   - keys within entity are ordered alphabetically, descending
;      - except for a few keys which are given preference (ex. :id, :type)
;   - UUIDs are stored as strings

(def entity-key-order [:id :type :slug :name :title])

(defn- key-compare [key1 key2]
  (let [key1 (keyword key1)
        key2 (keyword key2)
        order entity-key-order]
    (cond
      ; key1 and key2 in order
      (and (not= -1 (.indexOf order key1))
           (not= -1 (.indexOf order key2)))
      (compare
        (.indexOf order key1)
        (.indexOf order key2))

      ; key1 in order
      (not= -1 (.indexOf order key1))
      -1

      ; key2 in order
      (not= -1 (.indexOf order key2))
      1

      ; neither key in order
      :else
      (compare key1 key2))))

(extend-protocol yaml.reader/YAMLReader
  java.lang.String
  (yaml.reader/decode [data]
    (if (= 36 (count data))
      (try
        (java.util.UUID/fromString data)
        (catch java.lang.IllegalArgumentException e
          data))
      data)))

(defn- into-sorted-map [data]
  (into (sorted-map-by key-compare)
        (->> data
             (remove (fn [[k v]] (nil? v)))
             (map (fn [[k v]]
                    [(yaml.writer/encode k) (yaml.writer/encode v)])))))

(extend-protocol yaml.writer/YAMLWriter
  java.util.UUID
  (yaml.writer/encode [data]
    (yaml.writer/encode (str data)))

  clojure.lang.PersistentVector
  (yaml.writer/encode [data]
    (vec (sort (map yaml.writer/encode data))))

  clojure.lang.PersistentHashMap
  (yaml.writer/encode [data]
    (into-sorted-map data))

  flatland.ordered.map.OrderedMap
  (yaml.writer/encode [data]
    (into-sorted-map data))

  java.lang.Double
  (yaml.writer/encode [data]
    (if (zero? (mod data 1))
      (int data)
      data))

  clojure.lang.PersistentArrayMap
  (yaml.writer/encode [data]
    (into-sorted-map data)))

(defn from-string
  [s]
  (->> (yaml.reader/parse-string s)
       (into {})))

(defn to-string
  [entity]
  (-> entity
      ;; remove keys with vals of empty sequences
      (->> (remove (fn [[k v]]
                     (and (sequential? v) (empty? v))) )
           (into {}))
      (yaml.writer/generate-string :dumper-options {:flow-style :block})))

