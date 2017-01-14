(ns sculpture.specs.region
  (:require
    [clojure.spec :as s]
    [sculpture.specs.entity :refer [entity-type]]))

(s/def ::lat double?) ; TODO bound
(s/def ::lng double?) ; TODO bound

(s/def ::coordinates (s/tuple ::lng ::lat))
(s/def ::shape (s/coll-of ::coordinates :kind vector?))

(s/def ::polygon (s/nilable
                   (s/coll-of ::shape :kind vector?)))

(s/def ::name string?)

(s/def ::region
  (s/merge :sculpture.specs.entity/common
           (s/keys :req-un [::name
                            ::polygon])))

(defmethod entity-type "region"
  [_]
  ::region)
