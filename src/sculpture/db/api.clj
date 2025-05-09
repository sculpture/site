(ns sculpture.db.api
  (:require
   [malli.core :as m]
   [sculpture.config :refer [config]]
   [sculpture.db.api :as db]
   [sculpture.db.plain :as plain]
   [sculpture.db.github :as github]
   [sculpture.db.graph :as db.graph]
   [sculpture.db.pg.upsert :as db.upsert]
   [sculpture.schema.schema :as schema]
   [sculpture.schema.util :as schema.util]))

(def query db.graph/query)

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
   (schema/entity->attributes entity-type)))

#_(all-with-type "material")
#_(all-with-type "nationality")

(defn- push! [entity message author]
  {:pre [(m/validate schema/Entity entity)
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

(defn upsert! [entity user-id]
  {:pre [(m/validate schema/Entity entity)
         (uuid? user-id)
         (exists? "user" user-id)]}
  (let [entity-type (schema.util/entity-type entity)
        entity-id (schema.util/entity-id entity)
        action (if (exists? entity-type entity-id)
                 "Update"
                 "Add")]
    (db.upsert/upsert-entity! entity)
    (push! entity
           (str action " " entity-type " "
                (or (schema.util/entity-slug entity)
                    (schema.util/entity-id entity)))
           (query {:user/id user-id} [:user/name :user/email]))))

