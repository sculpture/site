(ns sculpture.db.api
  (:require
   [sculpture.config :refer [config]]
   [sculpture.db.plain :as plain]
   [sculpture.db.github :as github]
   [sculpture.db.graph :as db.graph]
   [sculpture.db.search :as db.search]
   [sculpture.db.postgres :as db.pg]
   [sculpture.db.datascript :as db.ds]
   [sculpture.schema.schema :as schema]
   [sculpture.schema.util :as schema.util]))

(defn query [& args]
  (apply db.graph/query args))

(defn ->id-key
  [entity-type]
  (keyword entity-type "id"))

(defn exists?
  [entity-type id]
  (boolean (seq (query
                 {(->id-key entity-type) id}
                 [(keyword entity-type "slug")]))))

#_(exists? "sculpture" #uuid "77ad0987-bf94-4619-85ed-047ef5677cd9")

(defn all-with-type
  [entity-type]
  (query
   (keyword (schema/pluralize entity-type))
   (schema/direct-attributes entity-type)))

#_(all-with-type "material")
#_(all-with-type "nationality")

(defn entity-counts []
  (->> (sculpture.schema.schema/types)
       (map (fn [entity-type]
              [entity-type
               (db.ds/q [:find '(count ?e) '.
                         :where ['?e (schema/id-key entity-type) '_]])]))))

#_(entity-counts)

(defn select-random-sculpture-slug []
  (db.ds/q '[:find (rand ?slug) .
            :where [_ :sculpture/slug ?slug]]))

#_(select-random-sculpture-slug)

(defn- push! [entity message author]
  {:pre [(schema/valid-entity? entity)
         (string? message)
         (:user/name author)
         (:user/email author)]}

  (github/update-file!
    (:github-repo config)
    (:github-repo-branch config)
    (plain/entity->path entity)
    {:content (plain/entity->yaml entity)
     :message message
     :committer {:name (:github-committer-name config)
                 :email (:github-committer-email config)}
     :author {:name (:user/name author)
              :email (:user/email author)}}))

#_(defn upsert! [entity user-id]
  {:pre [(schema/valid-entity? entity)
         (uuid? user-id)
         (exists? "user" user-id)]}
  (let [entity-type (schema.util/entity-type entity)
        entity-id (schema.util/entity-id entity)
        action (if (exists? entity-type entity-id)
                 "Update"
                 "Add")]
    (db.pg/upsert-sculpture! entity)
    (db.pg/upsert-region! entity)
    (push! entity
           (str action " " entity-type " "
                (or (schema.util/entity-slug entity)
                    (schema.util/entity-id entity)))
           (query {:user/id user-id} [:user/name :user/email]))))

(defn prep-for-datascript
  [entity]
  ;; when inserting into datascript
  ;; refs need to wrapped:
  ;;    {:sculpture/material-ids [1 2]}
  ;;  becomes:
  ;;    {:sculpture/material-ids [[:material/id 1] [:material/id 2]]}
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

(defn upsert! [entity]
  {:pre [(schema/valid-entity? entity)]}
  (db.ds/upsert-entity! (prep-for-datascript entity))
  (case (schema.util/entity-type entity)
    "sculpture"
    (db.pg/upsert-sculpture! entity)
    "region"
    (db.pg/upsert-region! entity)
    nil))

(defn search [& args]
  (apply db.search/search args))


