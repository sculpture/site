(ns sculpture.specs.core
  (:require
   [clojure.spec.alpha :as s]
   [sculpture.specs.entity]
   [sculpture.specs.sculpture]
   [sculpture.specs.city]
   [sculpture.specs.artist]
   [sculpture.specs.material]
   [sculpture.specs.user]
    [sculpture.specs.photo]
    [sculpture.specs.tag]
    [sculpture.specs.region]))

(s/def :sculpture/entity :sculpture.specs.entity/entity)
(s/def :sculpture/entity-type :sculpture.specs.entity/type)
(s/def :sculpture/user :sculpture.specs.user/user)
(s/def :sculpture/city :sculpture.specs.city/city-entity)
(s/def :sculpture/artist :sculpture.specs.artist/artist)
(s/def :sculpture/sculpture :sculpture.specs.sculpture/sculpture)
(s/def :sculpture/material :sculpture.specs.material/material)
(s/def :sculpture/photo :sculpture.specs.photo/photo)
(s/def :sculpture/region :sculpture.specs.region/region)
(s/def :sculpture/tag :sculpture.specs.tag/tag)

(defn demo []
  (s/explain :sculpture/entity
              {:id "123"
               :type "user"
               :email "foo@bar.com"
               :name "Rasd"}))



