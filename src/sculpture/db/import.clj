(ns sculpture.db.import
  (:require
    [sculpture.server.db :as data]
    [sculpture.db.core :as db]))

(defn import! []
  (println "Dropping...")
  (db/drop!)
  (println "Initializing...")
  (db/init!)

  (println "Inserting Artist Tags...")
  (doseq [artist-tag (data/search {:type "artist-tag"})]
    (db/insert-artist-tag! artist-tag))

  (println "Inserting Artists...")
  (doseq [artist (data/search {:type "artist"})]
    (db/insert-artist! artist))

  (println "Inserting Materials...")
  (doseq [material (data/search {:type "material"})]
    (db/insert-material! material))

  (println "Inserting Sculpture Tags...")
  (doseq [sculpture-tag (data/search {:type "sculpture-tag"})]
    (db/insert-sculpture-tag! sculpture-tag))

  (println "Inserting Sculptures...")
  (doseq [sculpture (data/search {:type "sculpture"})]
    (db/insert-sculpture! sculpture))

  (println "Inserting Region Tags...")
  (doseq [region-tag (data/search {:type "region-tag"})]
    (db/insert-region-tag! region-tag))

  (println "Inserting Regions...")
  (doseq [region (data/search {:type "region"})]
    (db/insert-region! region))

  (println "Inserting Users...")
  (doseq [user (data/search {:type "user"})]
    (db/insert-user! user))

  (println "Inserting Photos...")
  (doseq [photo (data/search {:type "photo"})]
    (db/insert-photo! photo)))
