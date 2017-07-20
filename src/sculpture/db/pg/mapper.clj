(ns sculpture.db.pg.mapper
  (:require
    [clojure.data.json :as json]
    [clojure.java.jdbc :as jdbc]
    [clj-time.coerce :as coerce]))

(def ->sql-date
  (comp
    coerce/to-sql-date
    coerce/from-date))

(def ->sql-time
  (comp
    coerce/to-sql-time
    coerce/from-date))

(extend-protocol jdbc/IResultSetReadColumn
  java.sql.Date
  (result-set-read-column [val _ _]
    (coerce/to-date (coerce/from-string (.toString val))))

  java.sql.Array
  (result-set-read-column [val _ _]
    (vec (remove nil? (into [] (.getArray val)))))

  org.postgresql.util.PGobject
  (result-set-read-column [pg-object _ _]
    (case (.getType pg-object)
      "json" (json/read-str (.getValue pg-object) :key-fn keyword)
      "geography" (let [geometry (org.postgis.PGgeometry/geomFromString (.getValue pg-object))]
                    (condp instance? geometry
                      org.postgis.Polygon
                      geometry

                      org.postgis.Point
                      {:latitude (.getY geometry)
                       :longitude (.getX geometry)})))))

(defn update-if-exists [obj k f]
  (if (contains? obj k)
    (update obj k f)
    obj))

(def blank-entities
  {"sculpture" {:id nil
                :type nil
                :title nil
                :slug nil
                :size nil
                :note nil
                :date nil
                :date-precision nil
                :commissioned-by nil
                :location nil}
   "photo" {:id nil
            :type nil
            :captured-at nil
            :user-id nil
            :colors nil
            :width nil
            :height nil
            :sculpture-id nil}
   "user" {:id nil
           :type nil
           :email nil
           :name nil
           :avatar nil}
   "artist" {:id nil
             :type nil
             :name nil
             :slug nil
             :gender nil
             :link-website nil
             :link-wikipedia nil
             :bio nil
             :birth-date nil
             :birth-date-precision nil
             :death-date nil
             :death-date-precision nil}})

; ->db

(defmulti ->db :type)

(defmethod ->db :default
  [entity]
  (merge (blank-entities (entity :type))
         entity))

(defmethod ->db "sculpture"
  [sculpture]
  (-> (blank-entities "sculpture")
      (merge sculpture)
      (update :date ->sql-date)
      (assoc :location-lng (:longitude (sculpture :location)))
      (assoc :location-lat (:latitude (sculpture :location)))
      (assoc :location-precision (:precision (sculpture :location)))
      (dissoc :location)))

(defmethod ->db "photo"
  [photo]
  (-> (blank-entities (photo :type))
      (merge photo)
      (update :captured-at ->sql-time)
      (update :colors json/write-str)))

(defmethod ->db "region"
  [region]
  (-> region
      (assoc :shape (region :geojson))
      (dissoc :geojson)))

; db->

(defmulti db-> :type)

(defmethod db-> :default
  [result]
  result)

(defmethod db-> "region"
  [result]
  (-> result
      (dissoc :shape)))

(defmethod db-> "sculpture"
  [result]
  (-> result
      (update-if-exists :location (fn [location]
                                    (when (result :location)
                                      (assoc location :precision (result :location-precision)))))
      (dissoc :location-precision)
      (update-if-exists :regions (fn [json]
                                   (->> json
                                        distinct ; dedupe, wasn't able to do it in SQL
                                        (map #(select-keys % [:slug :name]))
                                        (map db->))))
      (update-if-exists :photos (fn [json]
                                  (->> json
                                       (map #(select-keys % [:id]))
                                       (map db->))))
      (update-if-exists :artists (fn [json]
                                   (->> json
                                        (map #(select-keys % [:name :slug]))
                                        (map db->))))
      (update-if-exists :materials (fn [json]
                                     (->> json
                                          (map #(select-keys % [:name :slug]))
                                          (map db->))))))

