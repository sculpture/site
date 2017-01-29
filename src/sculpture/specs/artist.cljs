(ns sculpture.specs.artist
  (:require
    [clojure.spec :as s]
    [sculpture.specs.types]
    [sculpture.specs.entity :refer [entity-type]]))

(s/def ::gender #{"male"
                  "female"
                  "other"})
(s/def ::link-website (s/nilable :sculpture.specs.types/link-type))
(s/def ::link-wikipedia (s/nilable
                          (s/and
                          :sculpture.specs.types/link-type
                          ; TODO regex wikipedia
                          )))
(s/def ::name string?)
(s/def ::bio (s/nilable string?))
(s/def ::slug :sculpture.specs.types/slug-type)
(s/def ::tag-ids :sculpture.specs.types/related-ids-type)

(s/def ::birth-date (s/nilable :sculpture.specs.types/timestamp-type))
(s/def ::death-date (s/nilable :sculpture.specs.types/timestamp-type))

(s/def ::birth-date-accuracy (s/nilable int?))
(s/def ::death-date-accuracy (s/nilable int?))


(s/def ::artist
  (s/merge :sculpture.specs.entity/common
           (s/keys :req-un [::name
                            ::gender
                            ::link-website
                            ::link-wikipedia
                            ::bio
                            ::birth-date
                            ::birth-date-accuracy
                            ::death-date
                            ::death-date-accuracy
                            ::slug
                            ::tag-ids])))

(defmethod entity-type "artist"
  [_]
  ::artist)
