(ns sculpture.db.api
  (:require
   [bloom.commons.uuid :as uuid]
   [sculpture.db.pg.graph :as db.graph]))

(defn ->id-key
  [entity-type]
  (keyword entity-type "id"))

(def uuid-regex #"^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

(defn entity-by-id-or-slug
  [entity-type id-or-slug]
  (if (re-matches uuid-regex id-or-slug)
    ;; uuid
    (db.graph/query
     {(->id-key entity-type)
      (uuid/from-string id-or-slug)})
    ;; slug
    (db.graph/query
     {(keyword entity-type "slug")
      id-or-slug})))

(defn exists?
  [entity-type id]
  (boolean (seq (db.graph/query
                 {(->id-key entity-type) id}
                 [(keyword entity-type "slug")]))))

#_(exists? "sculpture" #uuid "77ad0987-bf94-4619-85ed-047ef5677cd9")
