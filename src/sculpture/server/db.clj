(ns sculpture.server.db
  (:require
    [clojure.spec.alpha :as s]
    [clojure.data :refer [diff]]
    [environ.core :refer [env]]
    [sculpture.specs.core]
    [sculpture.server.github :as github]
    [sculpture.server.yaml :as yaml]))

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
  (println "Fetching data from github")
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

(defn get-by-id [id]
  {:pre [(uuid? id)]}
  (@records id))

(defn user-exists? [id]
  {:pre [(uuid? id)]}
  (= "user" (:type (get-by-id id))))

(defn search
  "Given a map, return all entities matching all values in map"
  [kvs]
  {:pre [(map? kvs)]}
  (->> @records
       vals
       (filter (fn [e]
                 (every? true?
                         (map (fn [[k v]]
                                (if (vector? (get e k))
                                  (contains? (set (get e k)) v)
                                  (= v (get e k))))
                              kvs))))))

(defn select
  "Given a map, return entity matching all values in map"
  [kvs]
  {:pre [(map? kvs)]}
  (->> (search kvs)
       first))

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

(defn upsert! [entity user-id]
  {:pre [(s/valid? :sculpture/entity entity)
         (uuid? user-id)
         (user-exists? user-id)]}
  (let [action (if (@records (entity :id)) "Update" "Add")]
    (swap! records assoc (entity :id) entity)
    (push! (entity :type)
           (str action " " (entity :type) " " (or (entity :slug) (entity :id)))
           (get-by-id user-id)))
  true)

(defn delete! [entity user-id]
  {:pre [(s/valid? :sculpture/entity entity)
         (exists? (entity :id))
         (uuid? user-id)
         (user-exists? user-id)]}
  (swap! records dissoc (entity :id))
  (push! (entity :type)
         (str "Delete " (entity :type) " " (or (entity :slug) (entity :id)))
         (get-by-id user-id))
  true)

(defn clear! []
  (reset! records {})
  true)
