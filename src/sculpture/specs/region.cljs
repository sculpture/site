(ns sculpture.specs.region
  (:require
    [clojure.spec :as s]
    [sculpture.specs.entity :refer [entity-type]]))

(s/def ::bounds (s/keys :req-un [::east
                                 ::north
                                 ::south
                                 ::west]))

(s/def ::geojson (s/nilable (s/and
                              string?
                              ; TODO
                              )))
(s/def ::name string?)

(s/def ::region
  (s/merge :sculpture.specs.entity/common
           (s/keys :req-un [::bounds
                            ::geojson
                            ::name])))

(defmethod entity-type "region"
  [_]
  ::region)
