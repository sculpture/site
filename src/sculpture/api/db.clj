(ns sculpture.api.db
  (:require
    [clojure.data :refer [diff]]
    [sculpture.api.github :as github]
    [sculpture.api.yaml :as yaml]))

(def repo "sculpture/data")

(defonce records (atom []))

; HELPER FUNCTIONS

(defn- key-by-id [arr]
  (reduce (fn [memo i] (assoc memo (:id i) i)) {} arr))

; REMOTE

(defn- fetch-data []
  (->> (github/fetch-paths-in-dir "sculpture/data" "data/")
       (mapcat (fn [path]
                 (->> path
                      (github/fetch-file repo)
                      github/parse-file-content
                      yaml/from-string)))))

; PUBLIC FUNCTIONS

(defn init! []
  (reset! records (key-by-id (fetch-data)))
  nil)

(defn all []
  (vals @records))
