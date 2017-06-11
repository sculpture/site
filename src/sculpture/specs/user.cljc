(ns sculpture.specs.user
  (:require
    [clojure.spec.alpha :as s]
    [sculpture.specs.types]
    [sculpture.specs.entity :refer [entity-type]]))

(s/def ::email :sculpture.specs.types/email-type)
(s/def ::name string?)
(s/def ::avatar :sculpture.specs.types/url-type)

(s/def ::user
  (s/merge :sculpture.specs.entity/common
           (s/keys :req-un [::email
                            ::name]
                   :req-opt [::avatar])))

(defmethod entity-type "user"
  [_]
  ::user)
