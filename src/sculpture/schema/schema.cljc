(ns sculpture.schema.schema
  (:require
    [sculpture.schema.types :as types]
    #?@(:cljs
         [[sculpture.admin.state.api :refer [dispatch!]]])))

(declare by-id)

(defn lookup-on-find
  [entity-type]
  #?(:cljs
     (fn [id callback]
       (dispatch! [:state.core/remote-eql!
                   {(get-in by-id [entity-type :entity/id-key]) id}
                   [(get-in by-id [entity-type :entity/label-key])]
                   (fn [e]
                     (callback
                       {:title ((get-in by-id [entity-type :entity/label-key]) e)}))]))))

(defn lookup-on-search
  [entity-type]
  #?(:cljs
     (fn [query callback]
       (dispatch! [:state.search/remote-search! query [entity-type] callback]))))

(def id-opts
  {:default nil
   :schema.attr/type :db.type/uuid
   :schema.attr/unique? true
   :spec uuid?
   :input {:type :string
           :disabled true}})

(def slug-opts
  {:default ""
   :schema.attr/type :db.type/string
   :schema.attr/unique? true
   :spec types/Slug
   :input {:type :string}})

(def optional-flexdate-opts
  {:default nil
   :optional true
   :schema.attr/type :db.type/string
   :spec [:maybe types/FlexDate]
   :input {:type :flexdate}})

(def optional-link-opts
  {:default nil
   :optional true
   :schema.attr/type :db.type/string
   :spec [:maybe types/Url]
   :input {:type :url}})

(def required-string-opts
  {:default ""
   :schema.attr/type :db.type/string
   :spec types/NonBlankString
   :input {:type :string}})

(def optional-string-opts
  {:optional true
   :default nil
   :schema.attr/type :db.type/string
   :spec [:maybe types/NonBlankString]
   :input {:type :string}})

(defn tag-ids-opts-for [entity-type]
  (let [tag-entity-type (case entity-type
                          "photo" "photo-tag"
                          "sculpture" "sculpture-tag"
                          "region" "region-tag"
                          "artist" "artist-tag"
                          nil)]
  {:default []
   :spec types/RelatedIds
   :optional true
   :schema.attr/relation [:many tag-entity-type]
   :input {:type :multi-lookup
           :optional true
           :on-find (lookup-on-find tag-entity-type)
           :on-search (lookup-on-search tag-entity-type)}}))

(def entities
  ;; top-level order determines 'order of insertion into db'
  ;; using array-maps, for order of keys displayed in editor
  [{:entity/id "artist-tag"
    :entity/id-plural "artist-tags"
    :entity/id-key :artist-tag/id
    :entity/label "Artist Tag"
    :entity/label-plural "Artist Tags"
    :entity/label-key :artist-tag/name
    :entity/table "artist-tags"
    :entity/spec
    (array-map
      :artist-tag/id id-opts
      :artist-tag/slug slug-opts
      :artist-tag/name required-string-opts)}

   {:entity/id "nationality"
    :entity/id-plural "nationalities"
    :entity/id-key :nationality/id
    :entity/label "Nationality"
    :entity/label-plural "Nationalities"
    :entity/label-key :nationality/demonym
    :entity/table "nationalities"
    :entity/spec
    (array-map
      :nationality/id id-opts
      :nationality/slug slug-opts
      :nationality/nation required-string-opts
      :nationality/demonym required-string-opts)}

   {:entity/id "artist"
    :entity/id-plural "artists"
    :entity/id-key :artist/id
    :entity/label "Artist"
    :entity/label-plural "Artists"
    :entity/label-key :artist/name
    :entity/table "artists_with_related_ids"
    :entity/spec
    (array-map
      :artist/id id-opts
      :artist/slug slug-opts
      :artist/name required-string-opts
      ;; optional:
      :artist/gender {:default nil
                      :optional true
                      :schema.attr/type :db.type/string
                      :spec [:maybe [:enum "male" "female" "other"]]
                      :input {:type :enum
                              :options #{"" "male" "female" "other"}}}
      :artist/link-website optional-link-opts
      :artist/link-wikipedia optional-link-opts
      :artist/birth-date optional-flexdate-opts
      :artist/death-date optional-flexdate-opts
      :artist/bio {:default nil
                   :optional true
                   :schema.attr/type :db.type/string
                   :spec [:maybe types/NonBlankString]
                   :input {:type :string
                           :length :long}}
      ;; related:
      :artist/nationality-ids {:default []
                               :schema.attr/relation [:many "nationality"]
                               :optional true
                               :spec types/RelatedIds
                               :input {:type :multi-lookup
                                       :optional true
                                       :on-find (lookup-on-find "nationality")
                                       :on-search (lookup-on-search "nationality")}}
      :artist/artist-tag-ids (tag-ids-opts-for "artist"))}

   {:entity/id "city"
    :entity/id-plural "cities"
    :entity/id-key :city/id
    :entity/label "City"
    :entity/label-plural "Cities"
    :entity/label-key :city/city
    :entity/table "cities"
    :entity/spec
    (array-map
      :city/id id-opts
      :city/slug slug-opts
      :city/city required-string-opts
      :city/region required-string-opts
      :city/country required-string-opts)}

   {:entity/id "material"
    :entity/id-plural "materials"
    :entity/id-key :material/id
    :entity/label "Material"
    :entity/label-plural "Materials"
    :entity/label-key :material/name
    :entity/table "materials_with_related_ids"
    :entity/spec
    (array-map
      :material/id id-opts
      :material/slug slug-opts
      :material/name required-string-opts)}

   {:entity/id "category"
    :entity/id-plural "categories"
    :entity/id-key :category/id
    :entity/label "Category"
    :entity/label-plural "Categories"
    :entity/label-key :category/name
    :entity/table "categories"
    :entity/spec
    (array-map
      :category/id id-opts
      :category/slug slug-opts
      :category/name required-string-opts)}

   {:entity/id "sculpture-tag"
    :entity/id-plural "sculpture-tags"
    :entity/id-key :sculpture-tag/id
    :entity/label "Sculpture Tag"
    :entity/label-plural "Sculpture Tags"
    :entity/label-key :sculpture-tag/name
    :entity/table "sculpture-tags_with_counts"
    :entity/spec
    (array-map
      :sculpture-tag/id id-opts
      :sculpture-tag/slug slug-opts
      :sculpture-tag/name required-string-opts
      ;; optional:
      :sculpture-tag/category-id {:default nil
                                  :schema.attr/relation [:one "category"]
                                  :optional true
                                  :spec [:maybe uuid?]
                                  ;; wrap in a function so sub can be called later
                                  :input {:type :enum-lookup
                                          :options
                                          #?(:cljs
                                             (fn [callback]
                                               (dispatch! [:state.core/remote-eql!
                                                           :categories
                                                           [:category/id
                                                            :category/name]
                                                           (fn [entities]
                                                             (->> entities
                                                                  (sort-by :category/name)
                                                                  (map (fn [entity]
                                                                         [(:category/id entity)
                                                                          (:category/name entity)]))
                                                                  (into {})
                                                                  callback))]))
                                             :clj nil)}})}

   {:entity/id "sculpture"
    :entity/id-plural "sculptures"
    :entity/id-key :sculpture/id
    :entity/label "Sculpture"
    :entity/label-plural "Sculptures"
    :entity/label-key :sculpture/title
    :entity/table "sculptures_with_related_ids"
    :entity/spec
    (array-map
      :sculpture/id id-opts
      :sculpture/slug slug-opts
      :sculpture/title required-string-opts
      ;; optional:
      :sculpture/location {:default nil
                           :schema.attr/type nil ;; not storing in ds
                           :spec types/Location
                           :input {:type :location
                                   :geocode (fn [query callback]
                                              #?(:cljs
                                                 (dispatch! [:state.edit/geocode! query callback])))}}
      :sculpture/note {:default nil
                       :optional true
                       :schema.attr/type :db.type/string
                       :spec [:maybe types/NonBlankString]
                       :input {:type :string
                               :length :long}}
      :sculpture/commissioned-by optional-string-opts
      :sculpture/date optional-flexdate-opts
      :sculpture/display-date optional-string-opts
      :sculpture/size {:optional true
                       :default nil
                       :schema.attr/type :db.type/long
                       :spec [:maybe integer?]
                       :input {:type :integer}}
      :sculpture/link-wikipedia optional-link-opts
      ;; related:
      :sculpture/sculpture-tag-ids (tag-ids-opts-for "sculpture")
      :sculpture/city-id {:default nil
                          :schema.attr/relation [:one "city"]
                          :optional true
                          :spec [:maybe uuid?]
                          :input {:type :single-lookup
                                  :on-find (lookup-on-find "city")
                                  :on-search (lookup-on-search "city")}}
      :sculpture/artist-ids {:default []
                             :schema.attr/relation [:many "artist"]
                             :spec types/RelatedIds
                             :input {:type :multi-lookup
                                     :on-find (lookup-on-find "artist")
                                     :on-search (lookup-on-search "artist")}}
      :sculpture/material-ids {:default []
                               :schema.attr/relation [:many "material"]
                               :spec types/RelatedIds
                               :input {:type :multi-lookup
                                       :on-find (lookup-on-find "material")
                                       :on-search (lookup-on-search "material")}})}

   {:entity/id "segment"
    :entity/id-plural "segments"
    :entity/id-key :segment/id
    :entity/label "Segment"
    :entity/label-plural "Segments"
    :entity/label-key :segment/name
    :entity/table "segments"
    :entity/spec
    (array-map
      :segment/id id-opts
      :segment/slug slug-opts
      :segment/name required-string-opts
      :segment/sculpture-id {:default nil
                             :schema.attr/relation [:one "sculpture"]
                             :spec [:maybe uuid?]
                             :input {:type :single-lookup
                                     :on-find (lookup-on-find "sculpture")
                                     :on-search (lookup-on-search "sculpture")}})}

   {:entity/id "region-tag"
    :entity/id-plural "region-tags"
    :entity/id-key :region-tag/id
    :entity/label "Region Tag"
    :entity/label-plural "Region Tags"
    :entity/label-key :region-tag/name
    :entity/table "region-tags"
    :entity/spec
    (array-map
      :region-tag/id id-opts
      :region-tag/slug slug-opts
      :region-tag/name required-string-opts)}

   {:entity/id "region"
    :entity/id-plural "regions"
    :entity/id-key :region/id
    :entity/label "Region"
    :entity/label-plural "Regions"
    :entity/label-key :region/name
    :entity/table "regions_with_related_ids"
    :entity/spec
    (array-map
      :region/id id-opts
      :region/slug slug-opts
      :region/name required-string-opts
      ;; optional:
      :region/geojson {:default nil
                       :schema.attr/type nil ;; not storing in ds
                       :spec types/GeoJson
                       :input {:type :geojson
                               :simplify (fn [geojson callback]
                                           #?(:cljs (dispatch! [:state.edit/simplify! geojson callback])))
                               :get-shape (fn [query callback]
                                            #?(:cljs
                                               (dispatch! [:state.edit/get-shape! query callback])))}}
      ;; related:
      :region/region-tag-ids (tag-ids-opts-for "region"))}



   {:entity/id "user"
    :entity/id-plural "users"
    :entity/id-key :user/id
    :entity/label "User"
    :entity/label-plural "Users"
    :entity/label-key :user/name
    :entity/table "users"
    :entity/spec
    (array-map
      :user/id id-opts
      :user/name required-string-opts
      :user/email {:default ""
                   :schema.attr/unique? true
                   :schema.attr/type :db.type/string
                   :spec types/Email
                   :input {:type :email
                           :disabled true}}
      ;; optional:
      :user/avatar {:optional true
                    :default ""
                    :schema.attr/type :db.type/string
                    :spec [:maybe types/Url]
                    :input {:type :url
                            :disabled true}})}

   {:entity/id "photo"
    :entity/id-plural "photos"
    :entity/id-key :photo/id
    :entity/label "Photo"
    :entity/label-plural "Photos"
    :entity/label-key :photo/id
    :entity/table "photos"
    :entity/spec
    (array-map
      :photo/id id-opts
      :photo/featured? {:default nil
                        :optional true
                        :schema.attr/type :db.type/boolean
                        :spec boolean?
                        :input {:type :boolean}}
      :photo/captured-at {:default nil
                          :schema.attr/type :db.type/instant
                          :spec inst?
                          :input {:type :datetime}}
      :photo/colors {:default []
                     :schema.attr/type :db.type/string
                     :spec [:sequential types/Color]
                     :input {:type :string
                             :disabled true}}
      :photo/width {:default nil
                    :schema.attr/type :db.type/long
                    :spec integer?
                    :input {:type :integer
                            :disabled true}}
      :photo/height {:default nil
                     :schema.attr/type :db.type/long
                     :spec integer?
                     :input {:type :integer
                             :disabled true}}
      ;; related
      :photo/user-id {:default nil
                      :schema.attr/relation [:one "user"]
                      :spec uuid?
                      :input {:type :single-lookup
                              :on-find (lookup-on-find "user")
                              :on-search (lookup-on-search "user")}}
      :photo/sculpture-id {:default nil
                           :schema.attr/relation [:one "sculpture"]
                           :spec [:maybe uuid?]
                           :input {:type :single-lookup
                                   :on-find (lookup-on-find "sculpture")
                                   :on-search (lookup-on-search "sculpture")}}
      :photo/segment-id {:default nil
                         :schema.attr/relation [:one "segment"]
                         :optional true
                         :spec [:maybe uuid?]
                         :input {:type :radio-related
                                 :options-fn
                                 (fn [photo callback]
                                   #?(:cljs
                                      (dispatch! [:state.search/remote-advanced-search!
                                                  "segment"
                                                  [{:key :segment/sculpture-id
                                                    :option :equals?
                                                    :value (:photo/sculpture-id photo)}]
                                                  (fn [segments]
                                                    (callback
                                                      (concat [{:label "None"
                                                                :value nil}]
                                                              (->> segments
                                                                   (map (fn [segment]
                                                                          {:label (:segment/name segment)
                                                                           :value (:segment/id segment)}))))))])))}})}])

(def by-id
  (zipmap (map :entity/id entities)
          entities))

(def schema
  (->> entities
       (mapcat (fn [e]
                 [(:entity/id e) (:entity/spec e)]))
       (apply array-map)))

(def entity-types
  (set (map :entity/id entities)))

(def entity->attributes
  (->> entities
       (map (fn [e]
              [(:entity/id e)
               (->> (:entity/spec e)
                    (remove (fn [[_k v]]
                                (:schema.attr/relation? v)))
                    (mapv first))]))
       (into {})))

(def pluralize
  (zipmap (map :entity/id entities)
          (map :entity/id-plural entities)))

(def id-key
  (zipmap (map :entity/id entities)
          (map :entity/id-key entities)))

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

(defn ->malli-spec [entity-type]
  (into [:map]
        (->> (schema entity-type)
             (map (fn [[k v]]
                    [k {:optional (:optional v)} (:spec v)])))))

(def EntityType
  (into [:enum] entity-types))

(def Entity
  (into [:multi {:dispatch (fn [e]
                             (namespace (first (keys e))))}]
        (->> schema
             keys
             (map (fn [k]
                    [k (->malli-spec k)])))))

(def SpecFor
  (memoize ->malli-spec))

