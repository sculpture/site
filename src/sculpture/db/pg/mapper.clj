(ns sculpture.db.pg.mapper
  (:require
    [next.jdbc.result-set :refer [ReadableColumn]]
    [next.jdbc.date-time] ;; requiring this auto-converts datetimes
    [sculpture.json :as json]))

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




