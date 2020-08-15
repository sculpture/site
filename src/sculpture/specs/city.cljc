(ns sculpture.specs.city
  (:require
   [clojure.spec.alpha :as s]
   [sculpture.specs.types]
   [sculpture.specs.entity :refer [entity-type]]))

(s/def ::city string?)
(s/def ::region string?)
(s/def ::country string?)
(s/def ::slug :sculpture.specs.types/slug-type)

(s/def ::city-entity
  (s/merge :sculpture.specs.entity/common
           (s/keys :req-un [::city
                            ::region
                            ::country
                            ::slug])))

(defmethod entity-type "city"
  [_]
  ::city-entity)
