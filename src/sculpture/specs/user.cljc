(ns sculpture.specs.user
  (:require
    [clojure.spec.alpha :as s]
    [sculpture.specs.entity :refer [entity-type]]))

(s/def ::email :sculpture.specs.types/email-type)
(s/def ::name string?)

(s/def ::user
  (s/merge :sculpture.specs.entity/common
           (s/keys :req-un [::email
                            ::name])))

(defmethod entity-type "user"
  [_]
  ::user)
