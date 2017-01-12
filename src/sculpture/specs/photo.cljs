(ns sculpture.specs.photo
  (:require
    [clojure.spec :as s]
    [sculpture.specs.entity :refer [entity-type]]
    [sculpture.specs.types]))

(s/def ::captured-at :sculpture.specs.types/timestamp-type)
(s/def ::sculpture-id :sculpture.specs.types/uuid-type)
(s/def ::url (s/and
               string?
               ; TODO
               ))
(s/def ::user-id :sculpture.specs.types/uuid-type)

(s/def ::photo
  (s/merge :sculpture.specs.entity/common
           (s/keys :req-un [::captured-at
                            ::url
                            ::user-id]
                   ; TODO shouldn't be optional
                   :opt-un [::sculpture-id])))

(defmethod entity-type "photo"
  [_]
  ::photo)
