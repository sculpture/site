(ns sculpture.specs.photo
  (:require
    [clojure.spec.alpha :as s]
    [sculpture.specs.entity :refer [entity-type]]
    [sculpture.specs.types]))

(s/def ::captured-at :sculpture.specs.types/timestamp-type)
(s/def ::sculpture-id :sculpture.specs.types/uuid-type)
(s/def ::user-id :sculpture.specs.types/uuid-type)

(s/def ::color #(re-matches #"^#[0-9A-F]{6}$" %1))
(s/def ::colors (s/coll-of ::color :kind vector?))
(s/def ::width int?)
(s/def ::height int?)

(s/def ::photo
  (s/merge :sculpture.specs.entity/common
           (s/keys :req-un [::captured-at
                            ::user-id
                            ::colors
                            ::width
                            ::height]
                   ; TODO shouldn't be optional
                   :opt-un [::sculpture-id])))

(defmethod entity-type "photo"
  [_]
  ::photo)
