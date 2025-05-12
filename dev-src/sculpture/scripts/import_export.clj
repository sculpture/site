(ns sculpture.scripts.import-export
  (:require
   [sculpture.config :refer [config]]
   [sculpture.db.plain :as plain]
   [sculpture.db.github :as github]
   [sculpture.db.api :as db]
   [sculpture.db.datascript :as db.ds]
   [sculpture.db.postgres :as db.pg]
   [sculpture.schema.schema :as schema]
   [sculpture.schema.util :as schema.util]))

(defn all-from-files []
  (plain/read-many "./compare/git_latest/"))

(defn all-from-memory []
  (->> (sculpture.schema.schema/types)
       (map (fn [entity-type]
              (db/query
               (keyword (schema/pluralize entity-type))
               (schema/all-attributes entity-type))))
       (apply concat)))

(defn next-error [entities]
  (->> entities
       (pmap (fn [entity]
               (when-not (schema/valid-entity? entity)
                 (schema/explain schema/Entity entity))))
       (remove nil?)
       (take 1)
       clojure.pprint/pprint))

#_(next-error (all-from-files))
#_(next-error (all-from-memory))

(defn import! [entities]
  (let [grouped-entities (group-by schema.util/entity-type entities)]
    ;; have to insert entity-types in the correct order
    (doseq [entity-type (schema/types)]
      (println "Inserting" (str entity-type "s..."))
      (doseq [entity (grouped-entities entity-type)]
        (db/upsert! entity))
      (println (count (grouped-entities entity-type))))))

#_(import! (all-from-files))

(defn export! [dir]
  (->> (sculpture.schema.schema/types)
       (map (fn [entity-type]
              (println "Saving" entity-type "...")
              (->> (db/query
                    (keyword (sculpture.schema.schema/pluralize entity-type))
                    (schema/all-attributes entity-type))
                   (sculpture.db.plain/save-many! dir))))
       doall)
  nil)

#_(export! "compare/git_latest/")

(defn reload! []
  (println "Dropping...")
  (db.pg/drop!)
  (println "Initializing...")
  (db.pg/init!)
  (db.ds/reinit!)
  (import! (all-from-files)
           #_(github/fetch-archive!
               (:github-repo config)
               (:github-repo-branch config)))
  true)

#_(import-from-dir! (github/fetch-archive! "sculpture/data" "test"))


