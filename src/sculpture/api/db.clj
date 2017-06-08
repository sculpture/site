(ns sculpture.api.db
  (:require
    [clojure.spec.alpha :as s]
    [clojure.data :refer [diff]]
    [environ.core :refer [env]]
    [sculpture.specs.core]
    [sculpture.api.github :as github]
    [sculpture.api.yaml :as yaml]))

(def repo (env :github-repo))
(def branch (env :github-repo-branch))
(def committer {:name (env :github-committer-name)
                :email (env :github-committer-email)})

(defonce records (atom {}))

; HELPER FUNCTIONS

(defn- key-by-id [arr]
  (reduce (fn [memo i] (assoc memo (:id i) i)) {} arr))

(defn- type->path [entity-type]
  (str "data/" entity-type "s.yml"))

; REMOTE

(defn- push! [entity-type message author]
  {:pre [(s/valid? :sculpture/entity-type entity-type)
         (string? message)
         (s/valid? :sculpture/user author)]}
  (let [entities (->> @records
                      vals
                      (filter (fn [e]
                                (= entity-type (e :type)))))
        path (type->path entity-type)]
    (github/update-file! repo branch path
                         {:content (yaml/to-string entities)
                          :message message
                          :committer committer
                          :author {:name (author :name)
                                   :email (author :email)}})))

(defn- fetch-data []
  (->> (github/fetch-paths-in-dir repo branch "data/")
       (mapcat (fn [path]
                 (->> path
                      (github/fetch-file repo branch)
                      github/parse-file-content
                      yaml/from-string)))))

; PUBLIC FUNCTIONS

; READ

(defn exists? [id]
  {:pre [(uuid? id)]}
  (contains? @records id))

(defn select [id]
  {:pre [(uuid? id)]}
  (@records id))

(defn user-exists? [id]
  {:pre [(uuid? id)]}
  (= "user" (:type (select id))))

(defn all []
  (or (vals @records) []))

; MODIFY

(defn init! [user]
  {:pre [(s/valid? :sculpture/user user)]}
  (reset! records {})
  (swap! records assoc (user :id) user)
  true)

(defn load! []
  (reset! records (key-by-id (fetch-data)))
  true)

(defn update! [entity user-id]
  {:pre [(s/valid? :sculpture/entity entity)
         (exists? (entity :id))
         (uuid? user-id)
         (user-exists? user-id)]}
  (swap! records assoc (entity :id) entity)
  (push! (entity :type)
         (str "Update " (entity :type) " " (or (entity :slug) (entity :id)))
         (select user-id))
  true)

(defn insert! [entity user-id]
  {:pre [(s/valid? :sculpture/entity entity)
         (not (exists? (entity :id)))
         (uuid? user-id)
         (user-exists? user-id)]}
  (swap! records assoc (entity :id) entity)
  (push! (entity :type)
         (str "Add " (entity :type) " " (or (entity :slug) (entity :id)))
         (select user-id))
  true)

(defn delete! [entity user-id]
  {:pre [(s/valid? :sculpture/entity entity)
         (exists? (entity :id))
         (uuid? user-id)
         (user-exists? user-id)]}
  (swap! records dissoc (entity :id))
  (push! (entity :type)
         (str "Delete " (entity :type) " " (or (entity :slug) (entity :id)))
         (select user-id))
  true)

(defn clear! []
  (reset! records {})
  true)
