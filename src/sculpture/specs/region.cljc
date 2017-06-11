(ns sculpture.specs.region
  (:require
    [clojure.spec.alpha :as s]
    [sculpture.specs.types]
    [sculpture.specs.entity :refer [entity-type]]))

(s/def ::geojson (s/nilable any?)) ; TODO

(s/def ::name string?)
(s/def ::slug :sculpture.specs.types/slug-type)
(s/def ::tag-ids :sculpture.specs.types/related-ids-type)

(s/def ::region
  (s/merge :sculpture.specs.entity/common
           (s/keys :req-un [::name
                            ::slug]
                   :opt-un [::tag-ids
                            ::geojson])))

(defmethod entity-type "region"
  [_]
  ::region)
