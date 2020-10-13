(ns sculpture.specs.sculpture
  (:require
    [clojure.spec.alpha :as s]
    [sculpture.specs.entity :refer [entity-type]]
    [sculpture.specs.types]))

(s/def ::title string?)
(s/def ::note (s/nilable string?))
(s/def ::artist-ids :sculpture.specs.types/related-ids-type)
(s/def ::tag-ids :sculpture.specs.types/related-ids-type)
(s/def ::material-ids :sculpture.specs.types/related-ids-type)
(s/def ::date (s/nilable :sculpture.specs.types/flexdate-type))
(s/def ::commissioned-by (s/nilable string?))
(s/def ::location (s/nilable :sculpture.specs.types/location-type))
(s/def ::slug :sculpture.specs.types/slug-type)
(s/def ::size (s/nilable integer?))
(s/def ::link-wikipedia (s/nilable
                          (s/and
                          :sculpture.specs.types/url-type
                          ; TODO regex wikipedia
                          )))

(s/def ::sculpture
  (s/merge :sculpture.specs.entity/common
           (s/keys :req-un [::title
                            ::slug]
                   :opt-un [::size
                            ::note
                            ::date
                            ::artist-ids
                            ::commissioned-by
                            ::link-wikipedia
                            ::material-ids
                            ::location
                            ::city-id
                            ::tag-ids])))

(defmethod entity-type "sculpture"
  [_]
  ::sculpture)
