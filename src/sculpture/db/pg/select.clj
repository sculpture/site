(ns sculpture.db.pg.select
  (:require
   [clojure.string :as string]
   [hugsql.core :as hugsql]
   [sculpture.schema.schema :as schema]
   [sculpture.db.pg.config :refer [db-spec]]
   [sculpture.db.pg.mapper :refer [db->]]))

(hugsql/def-db-fns "sculpture/db/pg/sql/select.sql")

(def entity-type->db-table
  (zipmap (map :entity/id schema/entities)
          (map :entity/table schema/entities)))

(defn entity-counts []
  (-entity-counts @db-spec))

(defn search
  [{:keys [query limit types]}]
  (if (string/blank? query)
    (throw (ex-info "Query must not be nil or blank" {}))
    (->> (-search
           @db-spec
           ;; given input "a canadian monument"
           {:query query
            ;; will be: "*a canadian monument*
            :ilike-query query
            ;; will be: "a* & canadian* & monument*"
            :raw-tsquery (-> query
                             (string/split #" ")
                             (->>
                               (map (fn [s]
                                      (str s ":*")))
                               (string/join "&")))
            ;; will be: "canad* & monum*" (stemming stop words)
            :parsed-tsquery query
            :limit limit
            :types types})
         (map db->))))

#_(search {:query "Wood"
           :types ["sculpture" "artist"]})
#_(search {:query "John"
           :types ["artist"]
           :limit 5})
#_(search {:query "Can"
           :types ["nationality"]
           :limit 5})
#_(search {:query "end"
           :types ["sculpture"]
           :limit 5})
#_(search {:query "connection" ;; stemming
           :types ["sculpture"]
           :limit 5})
#_(search {:query "canad"
           :types ["sculpture"]
           :limit 5})
#_(search {:query "canad monum"
           :types ["sculpture"]
           :limit 5})
#_(search {:query "canadian bear"
           :types ["sculpture"]
           :limit 5})
#_(search {:query "elo"
           :types ["sculpture"]
           :limit 5})

; sculptures

(defn select-sculptures-for-region [slug]
  (->> (-select-sculptures-for-region
         @db-spec
         {:region-slug slug})
       (map db->)))

(defn select-sculptures-for-artist [artist-slug]
  (->> (-select-sculptures-for-artist
         @db-spec
         {:artist-slug artist-slug})
       (map db->)))

(defn select-sculptures-for-decade [decade]
  (->> (-select-sculptures-for-decade
        @db-spec
        {:date-start (str (/ decade 10) "*")
         :date-end (str (+ decade 9) "-12-31")})
       (map db->)))

(defn select-sculptures-for-artist-tag-slug [artist-tag-slug]
  (->> (-select-sculptures-for-artist-tag-slug
         @db-spec
         {:artist-tag-slug artist-tag-slug})
       (map db->)))

(defn select-sculptures-for-artist-gender [artist-gender]
  (->> (-select-sculptures-for-artist-gender
         @db-spec
         {:artist-gender artist-gender})
       (map db->)))

(defn select-sculptures-for-sculpture-tag-slug [sculpture-tag-slug]
  (->> (-select-sculptures-for-sculpture-tag-slug
         @db-spec
         {:sculpture-tag-slug sculpture-tag-slug})
       (map db->)))

(defn select-sculptures-for-material-slug [slug]
  (->> (-select-sculptures-for-material-slug
         @db-spec
         {:material-slug slug})
       (map db->)))

; sculpture

(defn select-sculpture-with-slug [slug]
  (->> (-select-sculpture-with-slug
         @db-spec
         {:slug slug})
       db->))

; user

(defn select-user-with-email [email]
  (->> (-select-user-with-email
         @db-spec
         {:email email})
       db->))

; entity

(defn select-all-with-type [entity-type]
  (->> (-select-all-with-type
         @db-spec
         {:type (entity-type->db-table entity-type)}
         {:quoting :ansi})
       (map db->)))

; misc

(defn select-random-sculpture-slug []
  (->> (-select-random-sculpture-slug @db-spec)
       :slug))

; sample

#_(select-random-sculpture-slug)

#_(-select-sculpture-with-slug
    @db-spec
    {:slug "bird-wings"})

#_(time
    (select-sculptures-for-region "canada"))
