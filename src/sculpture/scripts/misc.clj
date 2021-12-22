(ns sculpture.scripts.misc
  (:require
    [clojure.string :as string]
    [clojure.java.io :as io]
    [sculpture.db.core :as db]
    [sculpture.db.yaml :as yaml]
    [sculpture.db.pg.select :as db.select]))

(def base-path "../sculpture-data/")

(defn save-entity! [entity]
  (spit (str base-path (db/entity->path entity)) (db/entity->yaml entity)))

(defn slugify [s]
     (-> s
         (string/trim)
         (string/lower-case)
         (string/replace #"\s" "-")
         (string/replace #"-+" "-")
         (string/replace #"[^a-z0-9-]" "")))

(comment
  ;; replace yaml datetimes with string dates
  (->> (io/file "../sculpture-data/data/sculpture")
       file-seq
       (filter (fn [file]
                 (string/ends-with? (.getName file) ".yml")))
       (take 1)
       (map slurp)
       (map yaml/from-string)))


(comment
  ;; create new cities
  (let [existing-cities (->> (io/file "../sculpture-data/data/city")
                             file-seq
                             (filter (fn [file]
                                       (string/ends-with? (.getName file) ".yml")))
                             (map slurp)
                             (map yaml/from-string))
        new-cities [["Montreal" "Quebec" "Canada"]
                    ["Windsor" "Ontario" "Canada"]
                    ["Ottawa" "Ontario" "Canada"]
                    ["Kingston" "Ontario" "Canada"]
                    ["Barrie" "Ontario" "Canada"]
                    ["Oshawa" "Ontario" "Canada"]
                    ["Whitby" "Ontario" "Canada"]
                    ["Pickering" "Ontario" "Canada"]
                    ["Toronto" "Ontario" "Canada"]
                    ["Richmond Hill" "Ontario" "Canada"]
                    ["Markham" "Ontario" "Canada"]
                    ["Stouffville" "Ontario" "Canada"]
                    ["Kleinburg" "Ontario" "Canada"]
                    ["Mississauga" "Ontario" "Canada"]
                    ["Brampton" "Ontario" "Canada"]
                    ["Oakville" "Ontario" "Canada"]
                    ["Guelph" "Ontario" "Canada"]
                    ["Hamilton" "Ontario" "Canada"]
                    ["Brantford" "Ontario" "Canada"]
                    ["Waterloo" "Ontario" "Canada"]
                    ["Grimsby" "Ontario" "Canada"]
                    ["St. Catharines" "Ontario" "Canada"]
                    ["Halifax" "Nova Scotia" "Canada"]
                    ["Buffalo" "New York" "United States of America" ]
                    ["Hull" "Quebec" "Canada"]
                    ["Stratford" "Ontario" "Canada"]
                    ["St. Thomas" "Ontario" "Canada"]]
        existing-city-names (set (map :city existing-cities))]
    (doall
      (for [[city region country] new-cities]
        (when (not (contains? existing-city-names city))
          (let [entity {:id (java.util.UUID/randomUUID)
                        :type "city"
                        :city city
                        :region region
                        :country country
                        :slug (slugify (str city "-" region "-" country))}]
            (println "save-entity" entity)
            (save-entity! entity)))))))

(comment
  ;; update sculptures with cities
  (let [cities (->> (io/file "../sculpture-data/data/city")
                    file-seq
                    (filter (fn [file]
                              (string/ends-with? (.getName file) ".yml")))
                    (map slurp)
                    (map yaml/from-string))]
           (doseq [city cities]
             (println (city :city))
             (let [sculpture-ids (->> (db.select/select-sculptures-for-region (slugify (city :city)))
                                      (map :id))]
               (doseq [sculpture-id sculpture-ids]
                 (println "updating" sculpture-id)
                 (-> (yaml/from-string (slurp (str base-path "data/sculpture/" sculpture-id ".yml")))
                     (assoc :city-id (city :id))
                     (save-entity!)))))))

(comment
  ;; sculptures without cities
  (let [sculpture (->> (io/file "../sculpture-data/data/sculpture")
                           file-seq
                           (filter (fn [file]
                                     (string/ends-with? (.getName file) ".yml")))
                           (map slurp)
                           (map yaml/from-string))]
           (->> sculpture
                (remove :city-id)
                (map (juxt :title :id)))))
