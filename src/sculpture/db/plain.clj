(ns sculpture.db.plain
  (:require
    [clojure.set :as set]
    [clojure.java.io :as io]
    [clojure.string :as string]
    [sculpture.db.yaml :as yaml]
    [sculpture.schema.schema :as schema]
    [sculpture.schema.util :as schema.util]))

(defn entity->path [entity]
  (str "data/" (schema.util/entity-type entity)  "/" (schema.util/entity-id entity) ".yml"))

(defn strip-namespaces [entity]
  (->> entity
       (map (fn [[k v]]
              [(keyword (name k)) v]))
       (into {})))

#_(strip-namespaces {:foo/bar "123"})

(defn add-namespaces
  [entity entity-type]
  (->> entity
       (map (fn [[k v]]
              [(keyword entity-type (name k)) v]))
       (into {})))

#_(add-namespaces {:id "123"} "foo")

(defn entity->yaml [entity]
  (let [entity-type (schema.util/entity-type entity)]
    (-> (select-keys entity (schema.util/entity-keys entity-type))
        (strip-namespaces)
        (assoc :type entity-type)
        (set/rename-keys {(keyword (str entity-type "-tag-ids"))
                          :tag-ids})
        yaml/to-string)))

#_(entity->yaml {:sculpture/id 123})

(defn yaml->entity [s]
  (let [object (yaml/from-string s)
        entity-type (:type object)]
    (-> object
        (dissoc :type)
        (set/rename-keys {:tag-ids (keyword (str entity-type "-tag-ids"))})
        (add-namespaces entity-type))))

#_(yaml->entity (entity->yaml {:sculpture/id 123
                               :sculpture/location {:longitude 1
                                                    :latitude 1
                                                    :precision 50}}))

(defn save-to-file! [root-folder entity]
  (spit (str root-folder "/" (entity->path entity))
        (entity->yaml entity)))

(defn save-many! [root-folder entities]
  (.mkdir (io/file root-folder))
  (.mkdir (io/file root-folder "data"))
  (doseq [type (schema/types)]
    (.mkdir (io/file root-folder "data" type)))
  (doseq [entity entities]
    (try
      (save-to-file! root-folder entity)
      (catch Exception e
        (println (str "Could not save entity") {:entity entity})
        (throw e)))))

(defn read-many [root-folder]
  (->> (io/file root-folder)
       file-seq
       (filter (fn [file]
                 (string/ends-with? (.getName file) ".yml")))
       (map (fn [file]
              (yaml->entity (slurp file))))))


