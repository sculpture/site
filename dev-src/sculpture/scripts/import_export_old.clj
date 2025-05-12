(ns sculpture.scripts.import-export-old
  (:require
   [clojure.set]
   [sculpture.schema.schema]
   [sculpture.schema.util]
   [sculpture.db.plain]
   [sculpture.db.yaml]
   [sculpture.db.pg.select]))

(doseq [type (sculpture.schema.schema/types)]
  (.mkdir (clojure.java.io/file "./out/" "data" type)))

(->> (sculpture.schema.schema/types)
     (map (fn [entity-type]
            (->> (sculpture.db.pg.select/select-all-with-type entity-type)
                 (map (fn [entity]
                        (let [entity (sculpture.db.plain/add-namespaces entity entity-type)]
                          (spit (str "./out/" (sculpture.db.plain/entity->path entity))
                                (-> entity
                                    (select-keys (conj (sculpture.schema.util/entity-keys entity-type)
                                                       (keyword entity-type "tag-ids")))
                                    (sculpture.db.plain/strip-namespaces)
                                    (assoc :type entity-type)
                                    (clojure.set/rename-keys
                                     {(keyword (str entity-type "-tag-ids"))
                                      :tag-ids})
                                    sculpture.db.yaml/to-string)))))
                 doall)))
     doall)
