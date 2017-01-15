(ns sculpture.specs.region
  (:require
    [clojure.spec :as s]
    [sculpture.specs.types]
    [sculpture.specs.entity :refer [entity-type]]))

(s/def ::geojson (s/nilable any?)) ; TODO

(s/def ::name string?)
(s/def ::slug :sculpture.specs.types/slug-type)
(s/def ::tag-ids :sculpture.specs.types/related-ids-type)

(s/def ::region
  (s/merge :sculpture.specs.entity/common
           (s/keys :req-un [::name
                            ::geojson
                            ::slug
                            ::tag-ids])))

(defmethod entity-type "region"
  [_]
  ::region)
