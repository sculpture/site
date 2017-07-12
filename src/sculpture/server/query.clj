(ns sculpture.server.query
  (:require
    [clj-time.core :as t]
    [clj-time.coerce :as tc]
    [sculpture.server.db :as db]))

; HELPERS

(defn extend-sculpture [sculpture]
  (-> sculpture
      (assoc :photos (db/search {:type "photo"
                                 :sculpture-id (:id sculpture)}))
      (assoc :artists (map (fn [id]
                             (db/select {:type "artist"
                                         :id id}))
                           (sculpture :artist-ids)))
      (assoc :materials (map (fn [id]
                               (db/select {:type "material"
                                           :id id}))
                             (sculpture :material-ids)))
      (assoc :tags (map (fn [id]
                          (db/select {:type "sculpture-tag"
                                      :id id}))
                        (sculpture :tag-ids)))))

(defn extend-artist [artist]
  (-> artist
      (assoc :tags (map (fn [id]
                          (db/select {:type "artist-tag"
                                      :id id}))
                        (artist :tag-ids)))))

; QUERIES

; QUERIES - ENTITY

(defn entities-all []
  (db/all))

; QUERIES - ARTISTS

(defn artists-all []
  (db/search {:type "artist"}))

(defn artist-with-slug [slug]
  (->> (db/select {:type "artist"
                   :slug slug})
       extend-artist))


; QUERIES - SCULPTURES

(defn sculpture-with-slug [slug]
  (->> (db/select {:type "sculpture"
                   :slug slug})
       extend-sculpture))

(defn sculptures-for-artist [artist-slug]
  (let [artist (db/select {:type "artist"
                           :slug artist-slug})]
    (->> (db/search {:type "sculpture"
                     :artist-ids (artist :id)})
         (map extend-sculpture))))

(defn sculptures-in-decade [decade]
  (->> (db/search {:type "sculpture"})
       (filter (fn [sculpture]
                 (when (sculpture :date)
                   (t/within? (t/interval (t/date-time (Integer. decade))
                                          (t/date-time (+ (Integer. decade) 10)))
                     (tc/from-date (sculpture :date))))))
       (map extend-sculpture)))

(defn sculptures-with-artist-tag [artist-tag-slug]
  (let [tag (db/select {:type "artist-tag"
                        :slug artist-tag-slug})
        artists (db/search {:type "artist"
                            :tag-ids (tag :id)})]
    (->> artists
         (mapcat (fn [artist]
                   (db/search {:type "sculpture"
                               :artist-ids (artist :id)})))
         set
         (map extend-sculpture))))

(defn sculptures-with-sculpture-tag [sculpture-tag-slug]
  (let [tag (db/select {:type "sculpture-tag"
                        :slug sculpture-tag-slug})]
    (->> (db/search {:type "sculpture"
                     :tag-ids (tag :id)})
         (map extend-sculpture))))

(defn sculptures-with-artist-gender [artist-gender]
  (->> (db/search {:type "artist"
                   :gender artist-gender})
       (mapcat (fn [artist]
                 (db/search {:type "sculpture"
                             :artist-ids (artist :id)})))
       set
       (map extend-sculpture)))
