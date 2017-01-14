(ns sculpture.specs.region
  (:require
    [clojure.spec :as s]
    [sculpture.specs.types]
    [sculpture.specs.entity :refer [entity-type]]))

(s/def ::geojson (s/nilable any?)) ; TODO

(s/def ::name string?)
(s/def ::slug :sculpture.specs.types/slug-type)

(s/def ::region
  (s/merge :sculpture.specs.entity/common
           (s/keys :req-un [::name
                            ::geojson
                            ::slug])))

(defmethod entity-type "region"
  [_]
  ::region)
