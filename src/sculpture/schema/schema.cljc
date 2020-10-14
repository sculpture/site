(ns sculpture.schema.schema)

;; TODO migrate admin.views.pages.entity_editor/schema
;; TODO migrate all sculpture.specs.*

(def NonNilString
  [:and string?
   ;; TODO
   ])

(def Slug
  [:and string?
   ;; TODO
   ])

(def FlexDate
  [:and string?
    ;; TODO
   ])

(def Url
  [:and string?
   ;; TODO
   ])

(def Email
  [:and string?
   ;; TODO
   ])

(def Location
  [:map
   [:longitude [:and number?
                ;; TODO bounds
                ]]
   [:latitude [:and number?
               ;; TODO bounds
               ]]
   [:precision {:optional true}
    number?]])

(def GeoJson
  ;; TODO
  any?)

(def Color
  [:and string?
   ;; TODO
   ])

;; TODO mark optional/required
(def schema
  ;; using array-maps so that order of keys is preserved
  ;; on the top-level, for 'order of insertion into db'
  ;; within each type, for 'order of display in editor'
  (array-map
    "artist-tag"
    (array-map
      :id {:default ""
           :spec uuid?}
      :type {:default "artist-tag"
             :spec [:enum "artist-tag"]}
      :name {:default ""
             :spec NonNilString}
      :slug {:default ""
             :spec Slug})

    "artist"
    (array-map
      :id {:default ""
           :spec uuid?}
      :type {:default "artist"
             :spec [:enum "artist"]}
      :name {:default ""
             :spec NonNilString}
      :gender {:default nil
               :spec [:enum "male" "female" "other"]}
      :nationality {:default nil
                    :spec NonNilString}
      :link-website {:default nil
                     :spec Url}
      :link-wikipedia {:default nil
                       :spec Url}
      :birth-date {:default nil
                   :spec FlexDate}
      :death-date {:default nil
                   :spec FlexDate}
      :bio {:default ""
            :spec NonNilString}
      :slug {:default ""
             :spec Slug}
      :tag-ids {:default []
                :spec [:vector uuid?]})

    "city"
    (array-map
      :id {:default ""
           :spec uuid?}
      :type {:default "city"
             :spec [:enum "city"]}
      :city  {:default ""
             :spec NonNilString}
      :region {:default ""
             :spec NonNilString}
      :country {:default ""
                :spec NonNilString}
      :slug {:default ""
             :spec Slug})

    "material"
    (array-map
      :id  {:default ""
            :spec uuid?}
      :type {:default "material"
             :spec [:enum "material"]}
      :name {:default ""
             :spec NonNilString}
      :slug {:default ""
             :spec Slug})

    "sculpture-tag"
    (array-map
      :id {:default ""
           :spec uuid?}
      :type {:default "sculpture-tag"
             :spec [:enum "sculpture-tag"]}
      :name {:default ""
             :spec NonNilString}
      :slug {:default ""
             :spec Slug})

    "sculpture"
    (array-map
      :id {:default ""
           :spec uuid?}
      :type {:default "sculpture"
             :spec [:enum "sculpture"]}
      :title {:default ""
              :spec NonNilString}
      :artist-ids {:default []
                   :spec [:vector uuid?]}
      :commissioned-by {:default ""
                        :spec NonNilString}
      :material-ids {:default []
                     :spec [:vector uuid?]}
      :city-id {:default nil
                :spec uuid?}
      ;; ???
      :location {:default nil
                 :spec Location}
      :note {:default ""
             :spec NonNilString}
      :tag-ids {:default []
                :spec [:vector uuid?]}
      :slug {:default ""
             :spec Slug}
      :date {:default nil
             :spec FlexDate}
      :size {:default nil
             :spec integer?}
      :link-wikipedia {:default ""
                       :spec Url})

    "region-tag"
    (array-map
      :id {:default ""
           :spec uuid?}
      :type {:default "region-tag"
             :spec [:enum "region-tag"]}
      :name {:default ""
             :spec NonNilString}
      :slug {:default ""
             :spec Slug})

    "region"
    (array-map
      :id {:default ""
           :spec uuid?}
      :type {:default "region"
             :spec [:enum "region"]}
      :name {:default ""
             :spec NonNilString}
      :geojson {:default nil
                :spec GeoJson}
      :slug {:default ""
             :spec Slug}
      :tag-ids {:default []
                :spec [:vector uuid?]})

    "user"
    (array-map
      :id  {:default ""
            :spec uuid?}
      :type {:default "user"
             :spec [:enum "user"]}
      :name {:default ""
             :spec NonNilString}
      :email {:default ""
              :spec Email}
      :avatar {:default ""
               :spec Url})

    "photo"
    (array-map
      :id  {:default ""
            :spec uuid?}
      :type {:default "photo"
             :spec [:enum "photo"]}
      :sculpture-id {:default nil
                     :spec uuid?}
      :captured-at {:default nil
                    :spec inst?}
      :user-id uuid?
      :colors {:default []
               :spec [:vector Color]}
      :width {:default nil
              :spec integer?}
      :height {:default nil
               :spec integer?})))

(defn ->keys
  [entity-type]
  (->> (schema entity-type)
       keys))

(defn ->blank-entity
  [entity-type]
  (->> (schema entity-type)
       keys
       ;; mapcat + array-map preserves key order
       (mapcat (fn [k]
                 [k nil]))
       (apply array-map)))

(defn types []
  (keys schema))

(defn ->default-entity
  [entity-type]
  (->> (schema entity-type)
       ;; mapcat + array-map preserves key order
       (mapcat (fn [[k v]]
                 [k (:default v)]))
       (apply array-map)))


