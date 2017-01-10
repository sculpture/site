(ns sculpture.specs.tag
  (:require
    [clojure.spec :as s]
    [sculpture.specs.entity :refer [entity-type]]))

(s/def ::name string?)

(s/def ::tag
  (s/merge :sculpture.specs.entity/common
           (s/keys :req-un [::name])))

(defmethod entity-type "tag"
  [_]
  ::tag)
