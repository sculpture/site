(ns sculpture.scripts.misc
  (:require
    [bloom.commons.uuid :as uuid]
    [clojure.string :as string]
    [clojure.java.io :as io]
    [sculpture.db.core :as db]
    [sculpture.db.yaml :as yaml]
    [sculpture.db.pg.select :as db.select]
    [sculpture.db.pg.upsert :as db.upsert]))

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

(comment
  ;; nationalities

  ;; get from text on artist entities
  (->> (io/file "../sculpture-data/data/artist")
       file-seq
       (filter (fn [file]
                 (string/ends-with? (.getName file) ".yml")))
       (map slurp)
       (map yaml/from-string)
       (map :nationality)
       (filter string?)
       (mapcat (fn [s]
                 (string/split s #",| |-")))
       set)

  ;; export to files
  (->> (for [[demonym nation] {"American" "United States of America"
                               "Armenian" "Armenia"
                               "Australian" "Australia"
                               "Austrian" "Austria"
                               "Belgian" "Belgium"
                               "Bulgarian" "Bulgaria"
                               "Canadian" "Canada"
                               "Chilean" "Chile"
                               "Chinese" "China"
                               "Colombian" "Columbia"
                               "Costa-Rican" "Costa-Rica"
                               "Croatian" "Croatia"
                               "Cuban" "Cuba"
                               "Czech" "Chechia"
                               "Danish" "Denmark"
                               "Dutch" "Netherlands"
                               "English" "England"
                               "Finnish" "Finland"
                               "French" "France"
                               "German" "Germany"
                               "Greek" "Greece"
                               "Hungarian" "Hungary"
                               "Icelandic" "Iceland"
                               "Indian" "India"
                               "Iranian" "Iran"
                               "Iraqi" "Iraq"
                               "Irish" "Ireland"
                               "Israeli" "Israel"
                               "Italian" "Italy"
                               "Japanese" "Japan"
                               "Jewish" nil
                               "Korean" "Korea"
                               "Latvian" "Latvia"
                               "Lithuanian" "Lithuania"
                               "Mexican" "Mexico"
                               "Norwegian" "Norway"
                               "Pakistani" "Pakistan"
                               "Polish" "Poland"
                               "Portuguese" "Portugal"
                               "Romanian" "Romania"
                               "Russian" "Russia"
                               "Scottish" "Scotland"
                               "Serbian" "Serbia"
                               "Slovak" "Slovenia"
                               "South-African" "South-Africa"
                               "Spanish" "Spain"
                               "Swedish" "Sweden"
                               "Swiss" "Switzerland"
                               "Taiwanese" "Taiwan"
                               "Ukrainian" "Ukraine"
                               "Venezuelan" "Venezuela"
                               "Vietnamese" "Vietnam"
                               "Welsh" "Wales"
                               "Zimbabwean" "Zimbabwe"
                               "British" "United Kingdom"
                               "Flemish" "Flanders"}]
         {:id (uuid/random)
          :type "nationality"
          :slug (slugify demonym)
          :demonym demonym
          :nation nation})
       (map (partial db/save-to-file! "../sculpture-data"))
       dorun)

  ;; import from file
  (->> (io/file "../sculpture-data/data/nationality")
       file-seq
       (filter (fn [file]
                 (string/ends-with? (.getName file) ".yml")))
       (map slurp)
       (map yaml/from-string)
       (map db.upsert/upsert-entity!)
       dorun)

  #_(db.upsert/upsert-entity! (yaml/from-string (slurp "../sculpture-data/data/nationality/47f58352-ea8b-4f2a-a6da-ceca7d19da75.yml")))
  ;; modify artists
  (let [nationalities (db.select/select-all-with-type "nationality")
        nationality->id (zipmap
                          (map :demonym nationalities)
                          (map :id nationalities))]
    (->> (io/file "../sculpture-data/data/artist")
         file-seq
         (filter (fn [file]
                   (string/ends-with? (.getName file) ".yml")))
         (map slurp)
         (map yaml/from-string)
         (map (fn [artist]
                (assoc artist
                  :nationality-ids
                  (if-let [nstring (:nationality artist)]
                    (for [n (string/split nstring #", | ")]
                      (or (nationality->id n)
                          (throw (ex-info (str "Can't find" (pr-str n) " for " (:name artist)) {}))))
                    []))))
         (map (partial db/save-to-file! "../sculpture-data"))
         dorun))

  ;; remove original nationality
  (->> (io/file "../sculpture-data/data/artist")
       file-seq
       (filter (fn [file]
                 (string/ends-with? (.getName file) ".yml")))
       (map slurp)
       (map yaml/from-string)
       (map (fn [artist]
              (dissoc artist :nationality)))
       (map (partial db/save-to-file! "../sculpture-data"))
       dorun)

  ;; re-import artists

  (->> (io/file "../sculpture-data/data/artist")
       file-seq
       (filter (fn [file]
                 (string/ends-with? (.getName file) ".yml")))
       (map slurp)
       (map yaml/from-string)
       (map db.upsert/upsert-entity!)
       dorun)
  )
