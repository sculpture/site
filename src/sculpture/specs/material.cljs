(ns sculpture.specs.material
  (:require
    [clojure.spec :as s]
    [sculpture.specs.entity :refer [entity-type]]
    [sculpture.specs.types]))

(s/def ::name string?)
(s/def ::slug :sculpture.specs.types/slug-type)

(s/def ::material
  (s/merge :sculpture.specs.entity/common
           (s/keys :req-un [::name
                            ::slug])))

(defmethod entity-type "material"
  [_]
  ::material)
