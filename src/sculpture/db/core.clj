(ns sculpture.db.core
  (:require
    [clojure.spec.alpha :as s]
    [clojure.data :refer [diff]]
    [environ.core :refer [env]]
    [sculpture.specs.core]
    [sculpture.server.github :as github]
    [sculpture.server.yaml :as yaml]
    [sculpture.db.pg.core :as db]
    [sculpture.db.pg.select :as db.select]
    [sculpture.db.pg.upsert :as db.upsert]))

(def repo (env :github-repo))
(def branch (env :github-repo-branch))
(def committer {:name (env :github-committer-name)
                :email (env :github-committer-email)})

(defn- type->path [entity-type]
  (str "data/" entity-type "s.yml"))

(defn- push! [entity-type message author]
  {:pre [(s/valid? :sculpture/entity-type entity-type)
         (string? message)
         (s/valid? :sculpture/user author)]}
  (let [entities (db.select/select-all-with-type entity-type)
        path (type->path entity-type)]
    (github/update-file! repo branch path
                         {:content (yaml/to-string entities)
                          :message message
                          :committer committer
                          :author {:name (author :name)
                                   :email (author :email)}})))

(defn fetch-data []
  (println "Fetching data from github")
  (->> (github/fetch-paths-in-dir repo branch "data/")
       (mapcat (fn [path]
                 (->> path
                      (github/fetch-file repo branch)
                      github/parse-file-content
                      yaml/from-string)))))

(defn import! [entities]
  (let [grouped-entities (group-by :type entities)]
    (doseq [entity-type ["artist-tag"
                         "artist"
                         "material"
                         "sculpture-tag"
                         "sculpture"
                         "region-tag"
                         "region"
                         "user"
                         "photo"]]
      (println "Inserting" (str entity-type "s..."))
      (doseq [entity (grouped-entities entity-type)]
        (db.upsert/upsert-entity! entity)))))

(defn reload! []
  (println "Dropping...")
  (db/drop!)
  (println "Initializing...")
  (db/init!)
  (import! (fetch-data))
  true)

(defn upsert! [entity user-id]
  {:pre [(s/valid? :sculpture/entity entity)
         (uuid? user-id)
         (db.select/exists? "user" user-id)]}
  (let [action (if (db.select/exists? (entity :type) (entity :id)) "Update" "Add")]
    (db.upsert/upsert-entity! entity)
    (push! (entity :type)
           (str action " " (entity :type) " " (or (entity :slug) (entity :id)))
           (db.select/select-entity-with-id "user" user-id)))
  true)
