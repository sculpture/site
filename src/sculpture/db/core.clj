(ns sculpture.db.core
  (:require
    [clojure.data.json :as json]
    [clj-time.core :as t]
    [clj-time.coerce :as coerce]
    [environ.core :refer [env]]
    [yesql.core :as yesql]))

(def ^:dynamic db-spec (env :database-url))

(yesql/defquery -init! "sculpture/db/sql/init.sql")

(yesql/defquery -drop! "sculpture/db/sql/drop.sql")

(defn init! []
  (-init! {} {:connection db-spec}))

(defn drop! []
  (-drop! {} {:connection db-spec}))


; ----

(def date->sql-time
  (comp
    coerce/to-sql-time
    coerce/from-date))

(def sql-time->date
  (comp coerce/to-date
        coerce/from-sql-time))

(defn sql-point->point [sql-point]
  (let [point (org.postgis.PGgeometry/geomFromString (.getValue sql-point))]
    {:longitude (.getY point)
     :latitude (.getX point)}))

(defn parse-pgjson [pg-object]
  (json/read-str (.getValue pg-object) :key-fn keyword))

(defn sculpture->db
  [sculpture]
  {:id (sculpture :id)
   :title (sculpture :title)
   :slug (sculpture :slug)
   :size (sculpture :size)
   :note (sculpture :note)
   :date (-> (sculpture :date)
             date->sql-time)
   :date_precision (sculpture :date-precision)
   :commissioned_by (sculpture :commissioned-by)
   :location_lng (:longitude (sculpture :location))
   :location_lat (:latitude (sculpture :location))})

(defn db->sculpture
  [result]
  (-> result
      (update :location sql-point->point)))

(defn db->extended-sculpture
  [result]
  (-> result
      (update :regions (fn [json]
                         (->> json
                              parse-pgjson
                              (map db->region)
                              (map #(select-keys % [:slug :name])))))
      (update :photos (fn [json]
                        (->> json
                             parse-pgjson
                             (map db->photo)
                             (map #(select-keys % [:id])))))
      (update :artists (fn [json]
                         (->> json
                              parse-pgjson
                              (map db->artist)
                              (map #(select-keys % [:name :slug])))))
      (update :materials (fn [json]
                           (->> json
                                parse-pgjson
                                (map db->material)
                                (map #(select-keys % [:name :slug])))))
      db->sculpture))

(defn region->db
  [region]
  {:id (region :id)
   :name (region :name)
   :slug (region :slug)
   :shape (region :geojson)})

(defn db->region
  [result]
  {:id (result :id)
   :name (result :name)
   :slug (result :slug)
   :sculpture-count (result :sculpture-count)})

(defn photo->db
  [photo]
  {:id (photo :id)
   :user_id (photo :user-id)
   :sculpture_id (photo :sculpture-id)
   :width (photo :width)
   :height (photo :height)
   :colors (json/write-str (photo :colors))
   :captured_at (-> (photo :captured-at)
                    date->sql-time)})

(defn db->photo
  [result]
  result)

(defn user->db
  [user]
  {:id (user :id)
   :name (user :name)
   :email (user :email)
   :avatar (user :avatar)})

(defn artist->db
  [artist]
  {:id (artist :id)
   :name (artist :name)
   :slug (artist :slug)
   :gender (artist :gender)
   :link_website (artist :link-website)
   :link_wikipedia (artist :link-wikipedia)
   :bio (artist :bio)
   :birth_date (artist :birth_date)
   :birth_date_precision (artist :birth-date-precision)
   :death_date (artist :death-date)
   :death_date_precision (artist :death-date-precision)})

(defn db->artist
  [result]
  result)

(defn artist-tag->db
  [artist-tag]
  {:id (artist-tag :id)
   :name (artist-tag :name)
   :slug (artist-tag :slug)})

(defn material->db
  [material]
  {:id (material :id)
   :name (material :name)
   :slug (material :slug)})

(defn region-tag->db
  [region-tag]
  {:id (region-tag :id)
   :name (region-tag :name)
   :slug (region-tag :slug)})

(defn sculpture-tag->db
  [sculpture-tag]
  {:id (sculpture-tag :id)
   :name (sculpture-tag :name)
   :slug (sculpture-tag :slug)})

(defn db->artist-tag
  [result]
  {:id (result :id)
   :name (result :name)
   :slug (result :slug)})

(defn db->material
  [result]
  {:id (result :id)
   :name (result :name)
   :slug (result :slug)})

(defn db->region-tag
  [result]
  {:id (result :id)
   :name (result :name)
   :slug (result :slug)})

(defn db->sculpture-tag
  [result]
  {:id (result :id)
   :name (result :name)
   :slug (result :slug)})

; ----

(yesql/defqueries "sculpture/db/sql/insert_queries.sql")

(defn insert-sculpture! [sculpture]
  (-insert-sculpture
    (sculpture->db sculpture)
    {:connection db-spec})

  (doseq [artist-id (sculpture :artist-ids)]
    (-relate-artist-sculpture
      {:artist_id artist-id
       :sculpture_id (sculpture :id)}
      {:connection db-spec}))

  (doseq [tag-id (sculpture :tag-ids)]
    (-relate-sculpture-sculpture-tag
      {:sculpture_tag_id tag-id
       :sculpture_id (sculpture :id)}
      {:connection db-spec}))

  (doseq [material-id (sculpture :material-ids)]
    (-relate-material-sculpture
      {:material_id material-id
       :sculpture_id (sculpture :id)}
      {:connection db-spec})))

(defn insert-region! [region]
  (-insert-region
    (region->db region)
    {:connection db-spec})

  (doseq [tag-id (region :tag-ids)]
    (-relate-region-region-tag
      {:region_tag_id tag-id
       :region_id (region :id)}
      {:connection db-spec})))

(defn insert-photo! [photo]
  (-insert-photo
    (photo->db photo)
    {:connection db-spec}))

(defn insert-user! [user]
  (-insert-user
    (user->db user)
    {:connection db-spec}))

(defn insert-artist! [artist]
  (-insert-artist
    (artist->db artist)
    {:connection db-spec})

  (doseq [tag-id (artist :tag-ids)]
    (-relate-artist-artist-tag
      {:artist_tag_id tag-id
       :artist_id (artist :id)}
      {:connection db-spec})))

(defn insert-artist-tag! [artist-tag]
  (-insert-artist-tag
    (artist-tag->db artist-tag)
    {:connection db-spec}))

(defn insert-sculpture-tag! [sculpture-tag]
  (-insert-sculpture-tag
    (sculpture-tag->db sculpture-tag)
    {:connection db-spec}))

(defn insert-material! [material]
  (-insert-material
    (material->db material)
    {:connection db-spec}))

(defn insert-region-tag! [region-tag]
  (-insert-region-tag
    (region-tag->db region-tag)
    {:connection db-spec}))


; ---

(yesql/defqueries "sculpture/db/sql/queries.sql")

(defn select-sculptures-for-region [slug]
  (-select-sculptures-for-region
    {:region_slug slug}
    {:connection db-spec
     :row-fn db->extended-sculpture}))

(defn select-sculptures-for-artist [artist-slug]
  (time (-select-sculptures-for-artist
          {:artist_slug artist-slug}
          {:connection db-spec
           :row-fn db->extended-sculpture})))

(defn select-sculptures-for-decade [decade]
  (-select-sculptures-for-decade
    {:date_start (coerce/to-sql-time (t/date-time decade))
     :date_end (coerce/to-sql-time (t/date-time (+ decade 10)))}
    {:connection db-spec
     :row-fn db->extended-sculpture}))

(defn select-sculptures-for-artist-tag-slug [artist-tag-slug]
  (-select-sculptures-for-artist-tag-slug
    {:artist_tag_slug artist-tag-slug}
    {:connection db-spec
     :row-fn db->extended-sculpture}))

(defn select-sculptures-for-artist-gender [artist-gender]
  (-select-sculptures-for-artist-gender
    {:artist_gender artist-gender}
    {:connection db-spec
     :row-fn db->extended-sculpture}))

(defn select-sculptures-for-sculpture-tag-slug [sculpture-tag-slug]
  (-select-sculptures-for-sculpture-tag-slug
    {:sculpture_tag_slug sculpture-tag-slug}
    {:connection db-spec
     :row-fn db->extended-sculpture}))

(defn select-regions []
  (-select-regions
    {}
    {:connection db-spec
     :row-fn db->region}))

(defn select-artists []
  (-select-artists
    {}
    {:connection db-spec
     :row-fn db->artist}))

(defn select-artist-with-slug [slug]
  (-select-artist-with-slug
    {:slug slug}
    {:connection db-spec
     :result-set-fn first
     :row-fn db->artist}))

(defn select-sculpture-with-slug [slug]
  (-select-sculpture-with-slug
    {:slug slug}
    {:connection db-spec
     :result-set-fn first
     :row-fn db->extended-sculpture}))

