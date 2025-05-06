(ns sculpture.db.core
  (:require
    [malli.core :as m]
    [sculpture.config :refer [config]]
    [sculpture.db.plain :as plain]
    [sculpture.db.github :as github]
    [sculpture.db.pg.core :as db]
    [sculpture.db.api :as db.api]
    [sculpture.db.pg.select :as db.select]
    [sculpture.db.pg.graph :as db.graph]
    [sculpture.db.pg.upsert :as db.upsert]
    [sculpture.schema.schema :as schema]
    [sculpture.schema.util :as schema.util]))

(def repo (:github-repo config))
(def branch (:github-repo-branch config))
(def committer {:name (:github-committer-name config)
                :email (:github-committer-email config)})

(defn- push! [entity message author]
  {:pre [(m/validate schema/Entity entity)
         (string? message)
         (:user/name author)
         (:user/email author)]}

  (github/update-file! repo branch (plain/entity->path entity)
                       {:content (plain/entity->yaml entity)
                        :message message
                        :committer committer
                        :author {:name (:user/name author)
                                 :email (:user/email author)}}))

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
       (map db.api/all-with-type)
       (apply concat)
       #_(take 2)
       (plain/save-many! dir)))

#_(export-to-dir! "export-data")

(defn reload! []
  (println "Dropping...")
  (db/drop!)
  (println "Initializing...")
  (db/init!)
  (import-from-dir! (github/fetch-archive! repo branch))
  true)

#_(import-from-dir! (github/fetch-archive! "sculpture/data" "test"))

(defn upsert! [entity user-id]
  {:pre [(m/validate schema/Entity entity)
         (uuid? user-id)
         (db/exists? "user" user-id)]}
  (let [entity-type (schema.util/entity-type entity)
        entity-id (schema.util/entity-id entity)
        action (if (db/exists? entity-type entity-id)
                 "Update"
                 "Add")]
    (db.upsert/upsert-entity! entity)
    (push! entity
           (str action " " entity-type " "
                (or (schema.util/entity-slug entity)
                    (schema.util/entity-id entity)))
           (db.graph/query {:user/id user-id} [:user/name :user/email]))))
