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

