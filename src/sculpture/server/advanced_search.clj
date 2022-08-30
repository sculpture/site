(ns sculpture.server.advanced-search
  (:require
    [clojure.string :as string]
    [sculpture.db.pg.graph :as db.graph]
    [sculpture.schema.util :as schema.util]
    [sculpture.schema.schema :as schema]))

(defn entity-filter
  [conditions]
  (fn [entity]
    (every? true?
            (map (fn [condition]
                   (let [entity-value (entity (condition :key))
                         condition-value (condition :value)]
                     (case (condition :option)
                       :nil?
                       (nil? entity-value)

                       :includes?
                       (and
                         (string? entity-value)
                         (string? condition-value)
                         (string/includes? entity-value condition-value))
                       :equals?
                       (= entity-value condition-value)
                       :before?
                       (and
                         entity-value
                         condition-value
                         (< entity-value condition-value))
                       :after?
                       (and
                         entity-value
                         condition-value
                         (> entity-value condition-value))
                       :less-than?
                       (and
                         entity-value
                         condition-value
                         (< entity-value condition-value))
                       :greater-than?
                       (and
                         entity-value
                         condition-value
                         (> entity-value condition-value))
                       :empty?
                       (empty? entity-value)

                       :contains?
                       (contains? (set entity-value) condition-value)
                       :re-matches?
                       (and
                         (string? entity-value)
                         (string? condition-value)
                         (not (nil? (re-matches (re-pattern condition-value) entity-value))))

                       true)))
                 conditions))))

(defn filter-entities
  "example conditions: [{:key :artist/name :option :includes? :value x}]"
  [conditions entities]
  (if (and (seq conditions)
        (every? identity (map :key conditions))
        (every? identity (map :option conditions)))
    (->> entities
         (filter (entity-filter conditions)))
    []))

(defn search
  [entity-type conditions]
  (filter-entities
    conditions
    (let [k (keyword (schema/pluralize entity-type))]
      (k (db.graph/query
           k
           (vec (schema.util/entity-keys entity-type)))))))

#_(search "sculpture" [{:key :sculpture/title
                        :option :includes?
                        :value "oon"}])

#_(search "user" [{:key :sculpture/title
                   :option :includes?
                   :value "oon"}])
