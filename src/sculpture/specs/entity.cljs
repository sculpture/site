(ns sculpture.specs.entity
  (:require
    [clojure.spec :as s]
    [sculpture.specs.types]))

(s/def ::id :sculpture.specs.types/uuid-type)
(s/def ::type #{"sculpture"
                "material"
                "artist"
                "region"
                "tag"
                "photo"
                "user"})

(s/def ::common
  (s/keys :req-un [::id
                   ::type]))

(defmulti entity-type :type)

(s/def ::entity
  (s/multi-spec entity-type :type))
