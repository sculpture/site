(ns sculpture.specs.artist
  (:require
    [clojure.spec :as s]
    [sculpture.specs.types]
    [sculpture.specs.entity :refer [entity-type]]))

(s/def ::gender #{"male"
                  "female"
                  "other"})
(s/def ::link-website :sculpture.specs.types/link-type)
(s/def ::link-wikipedia (s/and
                          :sculpture.specs.types/link-type
                          ; TODO regex wikipedia
                          ))
(s/def ::name string?)
(s/def ::slug :sculpture.specs.types/slug-type)
(s/def ::tag-ids :sculpture.specs.types/related-ids-type)

(s/def ::artist
  (s/merge :sculpture.specs.entity/common
           (s/keys :req-un [::name
                            ::gender
                            ::link-website
                            ::link-wikipedia
                            ::slug
                            ::tag-ids])))

(defmethod entity-type "artist"
  [_]
  ::artist)