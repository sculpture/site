(ns sculpture.db.pg.select
  (:require
    [clj-time.core :as t]
    [clj-time.coerce :as coerce]
    [hugsql.core :as hugsql]
    [sculpture.db.pg.config :refer [db-spec]]
    [sculpture.db.pg.mapper :refer [db->]]))

(hugsql/def-db-fns "sculpture/db/pg/sql/select.sql")

(def entity-type->db-table
  {"user" "users"
   "material" "materials"
   "artist-tag" "artist-tags"
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
         {:date-start (coerce/to-sql-time (t/date-time decade))
          :date-end (coerce/to-sql-time (t/date-time (+ decade 10)))})
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

; regions

(defn select-regions []
  (->> (-select-regions db-spec)
       (map db->)))

; artist

(defn select-artist-with-slug [slug]
  (->> (-select-artist-with-slug
         db-spec
         {:slug slug})
       db->))

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
  (-exists?
    db-spec
    {:entity-type (entity-type->db-table entity-type)
     :id id}))

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

(defn select-all []
  (->> ["user"
        "material"
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
