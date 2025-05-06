(ns sculpture.scripts.import-export
  (:require
   [sculpture.config :refer [config]]
   [sculpture.db.plain :as plain]
   [sculpture.db.github :as github]
   [sculpture.db.pg.core :as db.postgres]
   [sculpture.db.api :as db]
   [sculpture.db.pg.upsert :as db.upsert]
   [sculpture.schema.schema :as schema]
   [sculpture.schema.util :as schema.util]))

(defn import-data! [entities]
  (let [grouped-entities (group-by schema.util/entity-type entities)]
    ;; have to do these in the correct order
    (doseq [entity-type (schema/types)]
      (println "Inserting" (str entity-type "s..."))
      (doseq [entity (grouped-entities entity-type)]
        (db.upsert/upsert-entity! entity))
      (println (count (grouped-entities entity-type))))))

(defn import-from-dir! [dir]
  (-> (plain/read-many dir)
      (import-data!)))

#_(import-from-dir! "export-data")

(defn export-to-dir! [dir]
  (->> (schema/types)
       (map db/all-with-type)
       (apply concat)
       #_(take 2)
       (plain/save-many! dir)))

#_(export-to-dir! "export-data")

(defn reload! []
  (println "Dropping...")
  (db.postgres/drop!)
  (println "Initializing...")
  (db.postgres/init!)
  (import-from-dir! (github/fetch-archive!
                      (:github-repo config)
                      (:github-repo-branch config)))
  true)

#_(import-from-dir! (github/fetch-archive! "sculpture/data" "test"))


