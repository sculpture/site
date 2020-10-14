(ns sculpture.db.core
  (:require
    [clojure.spec.alpha :as s]
    [clojure.string :as string]
    [clojure.data :refer [diff]]
    [environ.core :refer [env]]
    [sculpture.specs.core]
    [sculpture.db.github :as github]
    [sculpture.db.yaml :as yaml]
    [sculpture.db.pg.core :as db]
    [sculpture.db.pg.select :as db.select]
    [sculpture.db.pg.upsert :as db.upsert]
    [sculpture.schema.schema :as schema]))

(def repo (env :github-repo))
(def branch (env :github-repo-branch))
(def committer {:name (env :github-committer-name)
                :email (env :github-committer-email)})

(defn entity->path [entity]
  (str "data/" (entity :type)  "/" (entity :id) ".yml"))

(defn entity->yaml [entity]
  (->> (select-keys entity (schema/->keys (entity :type)))
       yaml/to-string))

(defn- push! [entity message author]
  {:pre [(s/valid? :sculpture/entity entity)
         (string? message)
         (s/valid? :sculpture/user author)]}

  (github/update-file! repo branch (entity->path entity)
                       {:content (entity->yaml entity)
                        :message message
                        :committer committer
                        :author {:name (author :name)
                                 :email (author :email)}}))

(defn export-to-dir! []
  (.mkdir (java.io.File. "data"))
  (doseq [type (schema/types)]
    (.mkdir (java.io.File. (str "data/" type))))
  (doseq [entity (db.select/select-all)]
    (spit (entity->path entity) (entity->yaml entity))))

(defn import-data! [entities]
  (let [grouped-entities (group-by :type entities)]
    (doseq [entity-type (schema/types)]
      (println "Inserting" (str entity-type "s..."))
      (doseq [entity (grouped-entities entity-type)]
        (db.upsert/upsert-entity! entity))
      (println (count (grouped-entities entity-type))))))

(defn import-from-dir! [dir]
  (->> (clojure.java.io/file dir)
       file-seq
       (filter (fn [file]
                 (string/ends-with? (.getName file) ".yml")))
       (map (fn [file]
              (yaml/from-string (slurp file))))
       import-data!))

(defn reload! []
  (println "Dropping...")
  (db/drop!)
  (println "Initializing...")
  (db/init!)
  (import-from-dir! (github/fetch-archive! repo branch))
  true)

(defn upsert! [entity user-id]
  {:pre [(s/valid? :sculpture/entity entity)
         (uuid? user-id)
         (db.select/exists? "user" user-id)]}
  (let [action (if (db.select/exists? (entity :type) (entity :id)) "Update" "Add")]
    (db.upsert/upsert-entity! entity)
    (push! entity
           (str action " " (entity :type) " " (or (entity :slug) (entity :id)))
           (db.select/select-entity-with-id "user" user-id))))
