(ns sculpture.db.pg.mapper
  (:require
    [next.jdbc.result-set :refer [ReadableColumn]]
    [next.jdbc.date-time] ;; requiring this auto-converts datetimes
    [sculpture.json :as json]
    [sculpture.schema.schema :as schema]
    [sculpture.schema.util :as schema.util]))

(defn parse-pg-geography [v]
  (let [geometry (.getGeometry v)]
    (condp instance? geometry
      org.postgis.Polygon
      geometry

      org.postgis.Point
      {:latitude (.getY geometry)
       :longitude (.getX geometry)})))

(defn parse-pg-object [v]
  (case (.getType v)
    "json" (json/decode (.getValue v))
    "jsonb" (json/decode (.getValue v))))

(defn parse-pg-array [v]
  (vec (remove nil? (into [] (.getArray v)))))

(extend-protocol ReadableColumn
  org.postgis.PGgeography
  (read-column-by-label [v label]
    (parse-pg-geography v))
  (read-column-by-index [v result-set-meta index]
    (parse-pg-geography v))

  org.postgresql.jdbc.PgArray
  (read-column-by-label [v label]
    (parse-pg-array v))
  (read-column-by-index [v result-set-meta index]
    (parse-pg-array v))

  org.postgresql.util.PGobject
  (read-column-by-label [v label]
    (parse-pg-object v))
  (read-column-by-index [v result-set-meta index]
    (parse-pg-object v)) )

(defn update-if-exists [obj k f]
  (if (contains? obj k)
    (update obj k f)
    obj))

(defn remove-namespaces [entity]
  (->> entity
       (map (fn [[k v]]
              [(keyword (name k)) v]))
       (into {})))

#_(remove-namespaces {:foo/bar 123})

(defn add-type [entity]
  (assoc entity :entity/type (schema.util/entity-type entity)))

; ->db

(defn tee [x] (prn x ) x)

(defmulti ->db (fn [entity]
                 (schema.util/entity-type entity)))

(defmethod ->db :default
  [entity]
  (-> (schema/->blank-entity (schema.util/entity-type entity))
      (merge entity)
      (add-type)
      (remove-namespaces)))

(defmethod ->db "sculpture"
  [sculpture]
  (-> (schema/->blank-entity "sculpture")
      (merge sculpture)
      (add-type)
      (remove-namespaces)
      (assoc :location-lng (:longitude (:sculpture/location sculpture)))
      (assoc :location-lat (:latitude (:sculpture/location sculpture)))
      (assoc :location-precision (:precision (:sculpture/location sculpture)))
      (dissoc :location)))

(defmethod ->db "photo"
  [photo]
  (-> (schema/->blank-entity "photo")
      (merge photo)
      (add-type)
      (remove-namespaces)
      (update :colors (fn [c]
                        (when c
                          (json/encode c))))))


; db->

(defmulti db-> :type)

(defmethod db-> :default
  [result]
  result)

(defmethod db-> "region"
  [result]
  (-> result
      (dissoc :shape)))

(defmethod db-> "artist"
  [result]
  (-> result
      (update-if-exists :tags (fn [json]
                                   (->> json
                                        (remove (fn [x]
                                                  (nil? (:slug x))))
                                        distinct
                                        (map db->))))
      (update-if-exists :nationalities (fn [json]
                                         (->> json
                                              (remove (fn [x]
                                                        (nil? (:slug x))))
                                              distinct
                                              (map db->))))))

(defmethod db-> "sculpture"
  [result]
  (-> result
      (update-if-exists :location (fn [location]
                                    (when (result :location)
                                      (assoc location :precision (result :location-precision)))))
      (dissoc :location-precision)
      (update-if-exists :regions (fn [json]
                                   (->> json
                                        (remove (fn [x]
                                                  (nil? (:slug x))))
                                        distinct
                                        (map #(select-keys % [:slug :name]))
                                        (map db->))))
      (update-if-exists :regions-nearby (fn [json]
                                          (->> json
                                               (remove (fn [x]
                                                         (nil? (:slug x))))
                                               distinct
                                               (map #(select-keys % [:slug :name]))
                                               (map db->))))
      (update-if-exists :photos (fn [json]
                                  (->> json
                                       (map #(select-keys % [:id]))
                                       (map db->))))
      (update-if-exists :sculpture-tags (fn [json]
                                          (->> json
                                               (remove nil?)
                                               (map #(select-keys % [:name :slug]))
                                               (map db->))))
      (update-if-exists :artists (fn [json]
                                   (->> json
                                        (map #(select-keys % [:name :slug]))
                                        (map db->))))
      (update-if-exists :materials (fn [json]
                                     (->> json
                                          (map #(select-keys % [:name :slug]))
                                          (map db->))))))

