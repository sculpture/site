(ns sculpture.specs.tag
  (:require
    [clojure.spec.alpha :as s]
    [sculpture.specs.types]
    [sculpture.specs.entity :refer [entity-type]]))

(s/def ::name string?)
(s/def ::slug :sculpture.specs.types/slug-type)

(s/def ::tag
  (s/merge :sculpture.specs.entity/common
           (s/keys :req-un [::name
                            ::slug])))

(defmethod entity-type "artist-tag"
  [_]
  ::tag)

(defmethod entity-type "sculpture-tag"
  [_]
  ::tag)

(defmethod entity-type "region-tag"
  [_]
  ::tag)
