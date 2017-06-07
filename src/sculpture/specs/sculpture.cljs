(ns sculpture.specs.sculpture
  (:require
    [clojure.spec :as s]
    [sculpture.specs.entity :refer [entity-type]]
    [sculpture.specs.types]))

(s/def ::title string?)
(s/def ::note (s/nilable string?))
(s/def ::artist-ids :sculpture.specs.types/related-ids-type)
(s/def ::tag-ids :sculpture.specs.types/related-ids-type)
(s/def ::material-ids :sculpture.specs.types/related-ids-type)
(s/def ::date (s/nilable :sculpture.specs.types/timestamp-type))
(s/def ::date-precision (s/nilable #{"year" "year-month" "year-month-day"}))
(s/def ::commissioned-by (s/nilable string?))
(s/def ::location (s/nilable :sculpture.specs.types/location-type))
(s/def ::slug :sculpture.specs.types/slug-type)
(s/def ::size (s/nilable integer?))

(s/def ::sculpture
  (s/merge :sculpture.specs.entity/common
           (s/keys :req-un [::title
                            ::slug]
                   :opt-un [::size
                            ::note
                            ::date
                            ::date-precision
                            ::artist-ids
                            ::commissioned-by
                            ::material-ids
                            ::location
                            ::tag-ids])))

(defmethod entity-type "sculpture"
  [_]
  ::sculpture)
