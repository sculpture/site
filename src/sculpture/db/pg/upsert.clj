(ns sculpture.db.pg.upsert
  (:require
    [hugsql.core :as hugsql]
    [sculpture.db.pg.config :refer [db-spec]]
    [sculpture.db.pg.mapper :refer [->db]]
    [sculpture.schema.util :as schema.util]))

(hugsql/def-db-fns "sculpture/db/pg/sql/upsert.sql")

(defn upsert-sculpture! [sculpture]
  (-upsert-sculpture!
    @db-spec
    (->db sculpture))

  (-delete-relations-artist-sculpture!
    @db-spec
    {:sculpture-id (:sculpture/id sculpture)})

  (doseq [artist-id (:sculpture/artist-ids sculpture)]
    (-relate-artist-sculpture!
      @db-spec
      {:artist-id artist-id
       :sculpture-id (:sculpture/id sculpture)}))

  (-delete-relations-sculpture-sculpture-tag!
    @db-spec
    {:sculpture-id (:sculpture/id sculpture)})

  (doseq [tag-id (:sculpture/sculpture-tag-ids sculpture)]
    (-relate-sculpture-sculpture-tag!
      @db-spec
      {:sculpture-tag-id tag-id
       :sculpture-id (:sculpture/id sculpture)}))

  (-delete-relations-material-sculpture!
    @db-spec
    {:sculpture-id (:sculpture/id sculpture)})

  (doseq [material-id (:sculpture/material-ids sculpture)]
    (-relate-material-sculpture!
      @db-spec
      {:material-id material-id
       :sculpture-id (:sculpture/id sculpture)})))

(defn upsert-region! [region]
  (-upsert-region!
    @db-spec
    (->db region))

  (-delete-relations-region-region-tag!
    @db-spec
    {:region-id (:region/id region)})

  (doseq [tag-id (:region/region-tag-ids region)]
    (-relate-region-region-tag!
      @db-spec
      {:region-tag-id tag-id
       :region-id (:region/id region)})))

(defn upsert-photo! [photo]
  (-upsert-photo!
    @db-spec
    (->db photo)))

(defn upsert-segment! [segment]
  (-upsert-segment!
    @db-spec
    (->db segment)))

(defn upsert-user! [user]
  (-upsert-user!
    @db-spec
    (->db user)))

(defn upsert-artist! [artist]
  (-upsert-artist!
    @db-spec
    (->db artist))

  (-delete-relations-artist-artist-tag!
    @db-spec
    {:artist-id (:artist/id artist)})

  (-delete-relations-artist-nationality!
    @db-spec
    {:artist-id (:artist/id artist)})

  (doseq [tag-id (:artist/artist-tag-ids artist)]
    (-relate-artist-artist-tag!
      @db-spec
      {:artist-tag-id tag-id
       :artist-id (:artist/id artist)}))

  (doseq [nationality-id (:artist/nationality-ids artist)]
    (-relate-artist-nationality!
      @db-spec
      {:nationality-id nationality-id
       :artist-id (:artist/id artist)})))

(defn upsert-artist-tag! [artist-tag]
  (-upsert-artist-tag!
    @db-spec
    (->db artist-tag)))

(defn upsert-nationality! [nationality]
  (-upsert-nationality!
    @db-spec
    (->db nationality)))

(defn upsert-category! [category]
  (-upsert-category!
    @db-spec
    (->db category)))

(defn upsert-sculpture-tag! [sculpture-tag]
  (-upsert-sculpture-tag!
    @db-spec
    (->db sculpture-tag)))

(defn upsert-material! [material]
  (-upsert-material!
    @db-spec
    (->db material)))

(defn upsert-region-tag! [region-tag]
  (-upsert-region-tag!
   @db-spec
   (->db region-tag)))

(defn upsert-city! [city]
  (-upsert-city!
   @db-spec
   (->db city)))

(defn upsert-entity! [entity]
  (let [upsert-fn! (case (schema.util/entity-type entity)
                     "artist-tag" upsert-artist-tag!
                     "artist" upsert-artist!
                     "city" upsert-city!
                     "category" upsert-category!
                     "material" upsert-material!
                     "sculpture-tag" upsert-sculpture-tag!
                     "nationality" upsert-nationality!
                     "segment" upsert-segment!
                     "sculpture" upsert-sculpture!
                     "region-tag" upsert-region-tag!
                     "region" upsert-region!
                     "user" upsert-user!
                     "photo" upsert-photo!)]
    (upsert-fn! entity)))

