(ns sculpture.db.api
  (:require
   [sculpture.config :refer [config]]
   [sculpture.db.graph :as db.graph]
   [sculpture.db.search :as db.search]
   [sculpture.db.postgres :as db.pg]
   [sculpture.db.datascript :as db.ds]
   [sculpture.db.plain :as db.plain]
   [sculpture.db.git :as db.git]
   [sculpture.schema.schema :as schema]
   [sculpture.schema.util :as schema.util]))

(defn query [& args]
  (apply db.graph/query args))

(defn exists?
  [entity-type id]
  (boolean (db.ds/q [:find '?e '.
                     :in '$ '?id
                     :where ['?e (schema/id-key entity-type) '?id]]
                    id)))

#_(exists? "sculpture" #uuid "77ad0987-bf94-4619-85ed-047ef5677cd9")

(defn all-with-type
  [entity-type]
  (query
   (keyword (schema/pluralize entity-type))
   (schema/all-attributes entity-type)))

#_(all-with-type "material")
#_(all-with-type "nationality")

(defn entity-counts []
  (->> (sculpture.schema.schema/types)
       (map (fn [entity-type]
              [entity-type
               (db.ds/q [:find '(count ?e) '.
                         :where ['?e (schema/id-key entity-type) '_]])]))
       (into {})))

#_(entity-counts)

(defn upsert! [entity]
  (db.ds/upsert-entity! entity)
  (case (schema.util/entity-type entity)
    "sculpture"
    (db.pg/upsert-sculpture! entity)
    "region"
    (db.pg/upsert-region! entity)
    nil))

(defn upsert-from-web!
  [entity user-id]
  {:pre [(schema/valid-entity? entity)
         (uuid? user-id)
         (exists? "user" user-id)]}
  (upsert! entity)
  (db.plain/save-to-file! (:data-dir config) entity)
  (db.git/upsert-entity! entity
                         (query {:user/id user-id}
                                [:user/name :user/email])))

(defn search [& args]
  (apply db.search/search args))
