(ns sculpture.schema.schema
  #?(:cljs
     (:require
       [sculpture.admin.state.core :refer [subscribe dispatch!]])))

;; TODO migrate all sculpture.specs.*

(defn lookup-on-find
  []
  #?(:cljs
     (fn [id callback]
       (callback @(subscribe [:get-entity id])))))

(defn lookup-on-search
  [type]
  #?(:cljs
     (fn [query callback]
       (callback @(subscribe [:sculpture.edit/related-entity-search type query])))))

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

(def id-opts
  {:default nil
   :spec uuid?
   :input {:type :string
           :disabled true}})

(def string-opts
  {:default ""
   :spec NonNilString
   :input {:type :string}})

(def slug-opts
  {:default ""
   :spec Slug
   :input {:type :string}})

(def long-string-opts
  {:default ""
   :spec NonNilString
   :input {:type :string
           :length :long}})

(defn type-opts-for [entity-type]
  (let [types #{"" "artist" "artist-tag"
                "city" "material" "photo"
                "region" "region-tag"
                "sculpture" "sculpture-tag"
                "user"}]
    {:default entity-type
     :spec (into [:enum] types)
     :input {:type :enum
             :options types
             :disabled true}}))

(defn tag-ids-opts-for [entity-type]
  {:default []
   :spec [:vector uuid?]
   :input {:type :multi-lookup
           :on-find (lookup-on-find)
           :on-search (lookup-on-search
                        (case entity-type
                          "photo" "photo-tag"
                          "sculpture" "sculpture-tag"
                          "region" "region-tag"
                          "artist" "artist-tag"
                          nil))}})

;; TODO mark optional/required
(def schema
  ;; using array-maps so that order of keys is preserved
  ;; on the top-level, for 'order of insertion into db'
  ;; within each type, for 'order of display in editor'
  (array-map
    "artist-tag"
    (array-map
      :id id-opts
      :type (type-opts-for "artist-tag")
      :name string-opts
      :slug slug-opts)

    "artist"
    (array-map
      :id id-opts
      :type (type-opts-for "artist")
      :name string-opts
      :gender {:default nil
               :spec [:enum "male" "female" "other"]
               :input {:type :enum
                       :options #{"" "male" "female" "other"}}}
      :nationality string-opts
      :link-website {:default nil
                     :spec Url
                     :input {:type :url}}
      :link-wikipedia {:default nil
                       :spec Url
                       :input {:type :url}}
      :birth-date {:default nil
                   :spec FlexDate
                   :input {:type :flexdate}}
      :death-date {:default nil
                   :spec FlexDate
                   :input {:type :flexdate}}
      :bio long-string-opts
      :slug slug-opts
      :tag-ids (tag-ids-opts-for "artist"))

    "city"
    (array-map
      :id id-opts
      :type (type-opts-for "city")
      :city string-opts
      :region string-opts
      :country string-opts
      :slug slug-opts)

    "material"
    (array-map
      :id id-opts
      :type (type-opts-for "material")
      :name string-opts
      :slug slug-opts)

    "sculpture-tag"
    (array-map
      :id id-opts
      :type (type-opts-for "sculpture-tag")
      :name string-opts
      :slug slug-opts)

    "sculpture"
    (array-map
      :id id-opts
      :type (type-opts-for "sculpture")
      :title string-opts
      :artist-ids {:default []
                   :spec [:vector uuid?]
                   :input {:type :multi-lookup
                           :on-find (lookup-on-find)
                           :on-search (lookup-on-search "artist")}}
      :commissioned-by string-opts
      :material-ids {:default []
                     :spec [:vector uuid?]
                     :input {:type :multi-lookup
                             :on-find (lookup-on-find)
                             :on-search (lookup-on-search "material")}}
      :city-id {:default nil
                :spec uuid?
                :input {:type :single-lookup
                        :on-find (lookup-on-find)
                        :on-search (lookup-on-search "city")}}
      :location {:default nil
                 :spec Location
                 :input {:type :location
                         :geocode (fn [query callback]
                                    #?(:cljs
                                       (dispatch! [:sculpture.edit/geocode query callback])))}}
      :note long-string-opts
      :tag-ids (tag-ids-opts-for "sculpture")
      :slug slug-opts
      :date {:default nil
             :spec FlexDate
             :input {:type :flexdate}}
      :size {:default nil
             :spec integer?
             :input {:type :integer}}
      :link-wikipedia {:default ""
                       :spec Url
                       :input {:type :url}})

    "region-tag"
    (array-map
      :id id-opts
      :type (type-opts-for "region-tag")
      :name string-opts
      :slug slug-opts)

    "region"
    (array-map
      :id id-opts
      :type (type-opts-for "region")
      :name string-opts
      :geojson {:default nil
                :spec GeoJson
                :input {:type :geojson
                        :simplify (fn [geojson callback]
                                    #?(:cljs (dispatch! [:sculpture.edit/simplify geojson callback])))
                        :get-shape (fn [query callback]
                                     #?(:cljs
                                        (dispatch! [:sculpture.edit/get-shape query callback])))}}
      :slug slug-opts
      :tag-ids (tag-ids-opts-for "region"))

    "user"
    (array-map
      :id id-opts
      :type (type-opts-for "user")
      :name string-opts
      :email {:default ""
              :spec Email
              :input {:type :email
                      :disabled true}}
      :avatar {:default ""
               :spec Url
               :input {:type :url
                       :disabled true}})

    "photo"
    (array-map
      :id id-opts
      :type (type-opts-for "photo")
      :sculpture-id {:default nil
                     :spec uuid?
                     :input {:type :single-lookup
                             :on-find (lookup-on-find)
                             :on-search (lookup-on-search "sculpture")}}
      :captured-at {:default nil
                    :spec inst?
                    :input {:type :datetime}}
      :user-id {:default nil
                :spec uuid?
                :input {:type :single-lookup
                        :on-find (lookup-on-find)
                        :on-search (lookup-on-search "user")}}
      :colors {:default []
               :spec [:vector Color]
               :input {:type :string ;; TODO
                       :disabled true}}
      :width {:default nil
              :spec integer?
              :input {:type :integer
                      :disabled true}}
      :height {:default nil
               :spec integer?
               :input {:type :integer
                       :disabled true}})))

(defn ->keys
  [entity-type]
  (->> (schema entity-type)
       keys))

(defn attrs []
  (->> schema
       vals
       (mapcat keys)
       set))

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

;; schema, just attrs, without types
(def schema-by-attr
  (->> schema
       vals
       (apply merge)))

(defn attr->input [k]
  (get-in schema-by-attr [k :input]))


