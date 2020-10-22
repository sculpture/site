(ns sculpture.db.pg.select
  (:require
   [hugsql.core :as hugsql]
   [sculpture.db.pg.config :refer [db-spec]]
   [sculpture.db.pg.mapper :refer [db->]]))

(hugsql/def-db-fns "sculpture/db/pg/sql/select.sql")

(def entity-type->db-table
  {"user" "users"
   "material" "materials_with_related_ids"
   "artist-tag" "artist-tags"
   "city" "cities"
   "sculpture-tag" "sculpture-tags"
   "region-tag" "region-tags"
   "photo" "photos"
   "region" "regions_with_related_ids"
   "artist" "artists_with_related_ids"
   "sculpture" "sculptures_with_related_ids"})

; sculptures

(defn select-sculptures-for-region [slug]
  (->> (-select-sculptures-for-region
         db-spec
         {:region-slug slug})
       (map db->)))

(defn select-sculptures-for-artist [artist-slug]
  (->> (-select-sculptures-for-artist
         db-spec
         {:artist-slug artist-slug})
       (map db->)))

(defn select-sculptures-for-decade [decade]
  (->> (-select-sculptures-for-decade
        db-spec
        {:date-start (str (/ decade 10) "*")
         :date-end (str (+ decade 9) "-12-31")})
       (map db->)))

(defn select-sculptures-for-artist-tag-slug [artist-tag-slug]
  (->> (-select-sculptures-for-artist-tag-slug
         db-spec
         {:artist-tag-slug artist-tag-slug})
       (map db->)))

(defn select-sculptures-for-artist-gender [artist-gender]
  (->> (-select-sculptures-for-artist-gender
         db-spec
         {:artist-gender artist-gender})
       (map db->)))

(defn select-sculptures-for-sculpture-tag-slug [sculpture-tag-slug]
  (->> (-select-sculptures-for-sculpture-tag-slug
         db-spec
         {:sculpture-tag-slug sculpture-tag-slug})
       (map db->)))

(defn select-sculptures-for-material-slug [slug]
  (->> (-select-sculptures-for-material-slug
         db-spec
         {:material-slug slug})
       (map db->)))

; sculpture

(defn select-sculpture-with-slug [slug]
  (->> (-select-sculpture-with-slug
         db-spec
         {:slug slug})
       db->))

; user

(defn select-user-with-email [email]
  (->> (-select-user-with-email
         db-spec
         {:email email})
       db->))

; entity

(defn exists? [entity-type id]
  (->> (-exists?
         db-spec
         {:type (entity-type->db-table entity-type)
          :id id}
         {:quoting :ansi})
       :exists))

(defn select-all-with-type [entity-type]
  (->> (-select-all-with-type
         db-spec
         {:type (entity-type->db-table entity-type)}
         {:quoting :ansi})
       (map db->)))

(defn select-entity-with-id [entity-type id]
  (-> (-select-entity-with-id
        db-spec
        {:entity-type (entity-type->db-table entity-type)
         :id id}
        {:quoting :ansi})
      db->))

(defn select-entity-with-slug [entity-type slug]
  (-> (-select-entity-with-slug
        db-spec
        {:entity-type (entity-type->db-table entity-type)
         :slug slug}
        {:quoting :ansi})
      db->))

(def uuid-regex #"^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

(defn select-entity-with-id-or-slug [entity-type id-or-slug]
  (if (re-matches uuid-regex id-or-slug)
    (select-entity-with-id entity-type (java.util.UUID/fromString id-or-slug))
    (select-entity-with-slug entity-type id-or-slug)))

(defn select-all []
  (->> ["user"
        "material"
        "city"
        "artist-tag"
        "sculpture-tag"
        "region-tag"
        "photo"
        "region"
        "artist"
        "sculpture"]
       (map select-all-with-type)
       (apply concat)))

; misc

(defn select-random-sculpture-slug []
  (->> (-select-random-sculpture-slug db-spec)
       :slug))
