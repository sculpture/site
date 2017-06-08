(ns sculpture.specs.artist
  (:require
    [clojure.spec.alpha :as s]
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

(s/def ::birth-date-precision (s/nilable int?))
(s/def ::death-date-precision (s/nilable int?))


(s/def ::artist
  (s/merge :sculpture.specs.entity/common
           (s/keys :req-un [::name
                            ::slug]
                   :req-opt [::gender
                             ::link-website
                             ::link-wikipedia
                             ::bio
                             ::birth-date
                             ::birth-date-precision
                             ::death-date
                             ::death-date-precision
                             ::tag-ids])))

(defmethod entity-type "artist"
  [_]
  ::artist)
