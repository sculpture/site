(ns sculpture.specs.user
  (:require
    [sculpture.specs.entity :refer [entity-type]]
    [clojure.spec :as s]))

(s/def ::email :sculpture.specs.types/email-type)
(s/def ::name string?)

(s/def ::user
  (s/merge :sculpture.specs.entity/common
           (s/keys :req-un [::email
                            ::name])))

(defmethod entity-type "user"
  [_]
  ::user)
