(ns sculpture.json
  (:require
    [jsonista.core :as j]))

(defn encode [o]
  (j/write-value-as-string o))

(defn decode [o]
  (-> o
      (j/read-value j/keyword-keys-object-mapper)))

