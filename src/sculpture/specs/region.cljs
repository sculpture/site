(ns sculpture.specs.region
  (:require
    [clojure.spec :as s]
    [sculpture.specs.entity :refer [entity-type]]))

(s/def ::geojson (s/nilable any?)) ; TODO

(s/def ::name string?)

(s/def ::region
  (s/merge :sculpture.specs.entity/common
           (s/keys :req-un [::name
                            ::geojson])))

(defmethod entity-type "region"
  [_]
  ::region)
