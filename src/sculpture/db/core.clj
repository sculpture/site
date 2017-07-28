(ns sculpture.db.core
  (:require
    [clojure.spec.alpha :as s]
    [clojure.data :refer [diff]]
    [environ.core :refer [env]]
    [sculpture.specs.core]
    [sculpture.db.github :as github]
    [sculpture.db.yaml :as yaml]
    [sculpture.db.pg.core :as db]
    [sculpture.db.pg.select :as db.select]
    [sculpture.db.pg.upsert :as db.upsert]))

(def repo (env :github-repo))
(def branch (env :github-repo-branch))
(def committer {:name (env :github-committer-name)
                :email (env :github-committer-email)})

(defn- type->path [entity-type]
  (str "data/" entity-type "s.yml"))

(defn entities->yaml [entities]
  (->> entities
       (map (fn [entity]
              (case (entity :type)
                "sculpture"
                (select-keys entity [:id :type :title :slug :size :note :date :date-precision :artist-ids :commissioned-by :material-ids :location :tag-ids])
                "material"
                (select-keys entity [:id :type :name :slug])
                "artist"
                (select-keys entity [:id :type :name :nationality :slug :gender :link-website :link-wikipedia :bio :birth-date :birth-date-precision :death-date :death-date-precision :tag-ids])
                "region"
                (select-keys entity [:id :type :name :slug :tag-ids :geojson])
                "photo"
                (select-keys entity [:id :type :captured-at :user-id :colors :width :height :sculpture-id])
                "user"
                (select-keys entity [:id :type :email :name :avatar])
                "sculpture-tag"
                (select-keys entity [:id :type :name :slug])
                "region-tag"
                (select-keys entity [:id :type :name :slug])
                "artist-tag"
                (select-keys entity [:id :type :name :slug]))))
       yaml/to-string))

(defn- push! [entity-type message author]
  {:pre [(s/valid? :sculpture/entity-type entity-type)
         (string? message)
         (s/valid? :sculpture/user author)]}
  (let [entities (db.select/select-all-with-type entity-type)
        path (type->path entity-type)]
    (github/update-file! repo branch path
                         {:content (entities->yaml entities)
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

(defn export! []
  (doseq [[entity-type entities] (->> (db.select/select-all)
                                      (group-by :type))]
    (let [path (type->path entity-type)]
      (spit path (entities->yaml entities)))))

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
