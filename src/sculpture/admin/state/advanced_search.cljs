(ns sculpture.admin.state.advanced-search
  (:require
    [clojure.string :as string]))

(defn get-results [db]
  (let [conditions (get-in db [:advanced-search :conditions])
        condition (first conditions)
        entity-filter (fn [entity]
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
                                     conditions)))]
    (if (and (seq conditions)
          (every? identity (map :key conditions))
          (every? identity (map :option conditions)))
      (->> db
           :data
           vals
           (filter entity-filter))
      [])))
