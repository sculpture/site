(ns sculpture.specs.sculpture
  (:require
    [clojure.spec :as s]
    [sculpture.specs.entity :refer [entity-type]]
    [sculpture.specs.types]))

(s/def ::title string?)
(s/def ::note (s/or :string string?
                    :nil nil?))
(s/def ::artist-ids :sculpture.specs.types/related-ids-type)
(s/def ::tag-ids :sculpture.specs.types/related-ids-type)
(s/def ::material-ids :sculpture.specs.types/related-ids-type)
(s/def ::year :sculpture.specs.types/year-type)
(s/def ::commissioned-by string?)
(s/def ::location :sculpture.specs.types/location-type)
(s/def ::slug :sculpture.specs.types/slug-type)

(s/def ::sculpture
  (s/merge :sculpture.specs.entity/common
           (s/keys :req-un [::artist-ids
                            ::commissioned-by
                            ::material-ids
                            ::location
                            ::note
                            ::tag-ids
                            ::title
                            ::year]
                   :opt-un [::slug])))

(defmethod entity-type "sculpture"
  [_]
  ::sculpture)
