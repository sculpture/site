(ns sculpture.scripts.import-export
  (:require
   [sculpture.config :refer [config]]
   [sculpture.db.plain :as plain]
   [sculpture.db.github :as github]
   [sculpture.db.api :as db]
   [sculpture.db.postgres :as db.pg]
   [sculpture.db.datalog :as db.dl]
   [sculpture.schema.schema :as schema]
   [sculpture.schema.util :as schema.util]))

(defn prep-for-datalog [entity]
  (->> entity
       (map (fn [[k v]]
              [k
               ;; wrap all ref uuids in [:entity/id uuid]
               (if-let [id-key (some-> (get-in schema/by-id [(namespace k) :entity/spec k :schema.attr/relation 1])
                                       schema/id-key)]
                 (cond
                   (uuid? v)
                   [id-key v]
                   (and (vector? v) (uuid? (first v)))
                   (mapv (fn [x] [id-key x]) v)
                   :else
                   (throw (ex-info "Whut?" {})))
                 v)]))
       (into {})))

(defn import-data! [entities]
  (let [grouped-entities (group-by schema.util/entity-type entities)]
    ;; have to do these in the correct order
    (doseq [entity-type (schema/types)]
      (println "Inserting" (str entity-type "s..."))
      (doseq [entity (grouped-entities entity-type)]
        (db.dl/upsert-entity! (prep-for-datalog entity))
        #_(db.upsert/upsert-entity! entity))
      (println (count (grouped-entities entity-type))))))

(defn import-from-dir! [dir]
  (-> (plain/read-many dir)
      (import-data!)))

#_(take 3 (plain/read-many "./compare/git/"))
#_(map prep-for-datalog (take 3 (plain/read-many "./compare/git/")))
#_(import-data! (plain/read-many "./compare/git/"))

#_(import-from-dir! "export-data")

(defn export-to-dir! [dir]
  (->> (sculpture.schema.schema/types)
       (map (fn [entity-type]
              (db/query
               (keyword (sculpture.schema.schema/pluralize entity-type))
               (schema/entity->attributes entity-type))))
       (apply concat)
       (sculpture.db.plain/save-many! "./out")))

#_(export-to-dir! "export-data")

(defn reload! []
  (println "Dropping...")
  (db.pg/drop!)
  (println "Initializing...")
  (db.pg/init!)
  (import-from-dir! (github/fetch-archive!
                      (:github-repo config)
                      (:github-repo-branch config)))
  true)

#_(import-from-dir! (github/fetch-archive! "sculpture/data" "test"))


