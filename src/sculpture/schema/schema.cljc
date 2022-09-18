(ns sculpture.schema.schema
  #?(:cljs
     (:require-macros
       [sculpture.schema.schema :refer [intern-entity-type-specs!]]))
  (:require
    [sculpture.schema.types :as types]
    [camel-snake-kebab.core :as csk]
    #?@(:cljs
         [[sculpture.admin.state.api :refer [dispatch!]]])))

(def pluralize
  {"artist" "artists"
   "artist-tag" "artist-tags"
   "category" "categories"
   "city" "cities"
   "material" "materials"
   "nationality" "nationalities"
   "photo" "photos"
   "region" "regions"
   "region-tag" "region-tags"
   "sculpture" "sculptures"
   "sculpture-tag" "sculpture-tags"
   "user" "users"})

(def entity-types
  #{"artist"
    "artist-tag"
    "category"
    "city"
    "material"
    "nationality"
    "photo"
    "region"
    "region-tag"
    "sculpture"
    "sculpture-tag"
    "user"})

(def label-key
  {"artist" :artist/name
   "artist-tag" :artist-tag/name
   "category" :category/name
   "city" :city/city
   "material" :material/name
   "nationality" :nationality/demonym
   "photo" :photo/id
   "region" :region/name
   "region-tag" :region-tag/name
   "sculpture" :sculpture/title
   "sculpture-tag" :sculpture-tag/name
   "user" :user/name})

(def id-key
  {"artist" :artist/id
   "artist-tag" :artist-tag/id
   "category" :category/id
   "city" :city/id
   "material" :material/id
   "nationality" :nationality/id
   "photo" :photo/id
   "region" :region/id
   "region-tag" :region-tag/id
   "sculpture" :sculpture/id
   "sculpture-tag" :sculpture-tag/id
   "user" :user/id})

(defn lookup-on-find
  [entity-type]
  #?(:cljs
     (fn [id callback]
       (dispatch! [:state.core/remote-eql!
                   {(keyword entity-type "id") id}
                   [(label-key entity-type)]
                   (fn [e]
                     (callback
                       {:title ((label-key entity-type) e)}))]))))

(defn lookup-on-search
  [entity-type]
  #?(:cljs
     (fn [query callback]
       (dispatch! [:state.search/remote-search! query [entity-type] callback]))))

(def id-opts
  {:default nil
   :spec uuid?
   :input {:type :string
           :disabled true}})

(def slug-opts
  {:default ""
   :spec types/Slug
   :input {:type :string}})

(def EntityType
  (into [:enum] entity-types))

(defn tag-ids-opts-for [entity-type]
  {:default []
   :spec types/RelatedIds
   :input {:type :multi-lookup
           :optional true
           :on-find (lookup-on-find
                      (case entity-type
                          "photo" "photo-tag"
                          "sculpture" "sculpture-tag"
                          "region" "region-tag"
                          "artist" "artist-tag"
                          nil))
           :on-search (lookup-on-search
                        (case entity-type
                          "photo" "photo-tag"
                          "sculpture" "sculpture-tag"
                          "region" "region-tag"
                          "artist" "artist-tag"
                          nil))}})

(def schema
  ;; using array-maps so that order of keys is preserved
  ;; on the top-level, for 'order of insertion into db'
  ;; within each type, for 'order of display in editor'
  (array-map
    "artist-tag"
    (array-map
      :artist-tag/id id-opts
      :artist-tag/slug slug-opts
      :artist-tag/name {:default ""
                        :spec types/NonBlankString
                        :input {:type :string}})

    "nationality"
    (array-map
      :nationality/id id-opts
      :nationality/slug slug-opts
      :nationality/nation {:default ""
                           :spec types/NonBlankString
                           :input {:type :string}}
      :nationality/demonym {:default ""
                            :spec types/NonBlankString
                            :input {:type :string}})

    "artist"
    (array-map
      :artist/id id-opts
      :artist/slug slug-opts
      :artist/name {:default ""
                    :spec types/NonBlankString
                    :input {:type :string}}
      ;; optional:
      :artist/gender {:default nil
                      :optional true
                      :spec [:maybe [:enum "male" "female" "other"]]
                      :input {:type :enum
                              :options #{"" "male" "female" "other"}}}
      :artist/link-website {:default nil
                            :optional true
                            :spec [:maybe types/Url]
                            :input {:type :url}}
      :artist/link-wikipedia {:default nil
                              :optional true
                              :spec [:maybe types/Url]
                              :input {:type :url}}
      :artist/birth-date {:default nil
                          :optional true
                          :spec [:maybe types/FlexDate]
                          :input {:type :flexdate}}
      :artist/death-date {:default nil
                          :optional true
                          :spec [:maybe types/FlexDate]
                          :input {:type :flexdate}}
      :artist/bio {:default nil
                   :optional true
                   :spec [:maybe types/NonBlankString]
                   :input {:type :string
                           :length :long}}
      ;; related:
      :artist/nationality-ids {:default []
                               :spec types/RelatedIds
                               :input {:type :multi-lookup
                                       :optional true
                                       :on-find (lookup-on-find "nationality")
                                       :on-search (lookup-on-search "nationality")}}
      :artist/artist-tag-ids (tag-ids-opts-for "artist"))

    "city"
    (array-map
      :city/id id-opts
      :city/slug slug-opts
      :city/city {:default ""
                  :spec types/NonBlankString
                  :input {:type :string}}
      :city/region {:default ""
                    :spec types/NonBlankString
                    :input {:type :string}}
      :city/country {:default ""
                     :spec types/NonBlankString
                     :input {:type :string}})

    "material"
    (array-map
      :material/id id-opts
      :material/slug slug-opts
      :material/name {:default ""
                      :spec types/NonBlankString
                      :input {:type :string}})

    "category"
    (array-map
      :category/id id-opts
      :category/slug slug-opts
      :category/name {:default ""
                      :spec types/NonBlankString
                      :input {:type :string}})

    "sculpture-tag"
    (array-map
      :sculpture-tag/id id-opts
      :sculpture-tag/slug slug-opts
      :sculpture-tag/name {:default ""
                           :spec types/NonBlankString
                           :input {:type :string}}
      ;; optional:
      :sculpture-tag/category-id {:default nil
                                  :optional true
                                  :spec [:maybe uuid?]
                                  ;; wrap in a function so sub can be called later
                                  :input (fn []
                                           {:type :enum-lookup
                                            :options
                                            #?(:cljs (let [categories (atom nil)]
                                                       (->> (dispatch! [:state.core/remote-eql!
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
                                                                               (reset! categories)))])))
                                               :clj nil)})})

    "sculpture"
    (array-map
      :sculpture/id id-opts
      :sculpture/slug slug-opts
      :sculpture/title {:default ""
                        :spec types/NonBlankString
                        :input {:type :string}}
      ;; optional:
      :sculpture/location {:default nil
                           :spec types/Location
                           :input {:type :location
                                   :geocode (fn [query callback]
                                              #?(:cljs
                                                 (dispatch! [:state.edit/geocode! query callback])))}}
      :sculpture/note {:default nil
                       :optional true
                       :spec [:maybe types/NonBlankString]
                       :input {:type :string
                               :length :long}}
      :sculpture/commissioned-by {:optional true
                                  :default ""
                                  :spec [:maybe types/NonBlankString]
                                  :input {:type :string}}
      :sculpture/date {:optional true
                       :default nil
                       :spec [:maybe types/FlexDate]
                       :input {:type :flexdate}}
      :sculpture/size {:optional true
                       :default nil
                       :spec [:maybe integer?]
                       :input {:type :integer}}
      :sculpture/link-wikipedia {:optional true
                                 :default nil
                                 :spec [:maybe types/Url]
                                 :input {:type :url}}
      ;; related:
      :sculpture/sculpture-tag-ids (tag-ids-opts-for "sculpture")
      :sculpture/city-id {:default nil
                          :optional true
                          :spec [:maybe uuid?]
                          :input {:type :single-lookup
                                  :on-find (lookup-on-find "city")
                                  :on-search (lookup-on-search "city")}}
      :sculpture/artist-ids {:default []
                             :spec types/RelatedIds
                             :input {:type :multi-lookup
                                     :on-find (lookup-on-find "artist")
                                     :on-search (lookup-on-search "artist")}}
      :sculpture/material-ids {:default []
                               :spec types/RelatedIds
                               :input {:type :multi-lookup
                                       :on-find (lookup-on-find "material")
                                       :on-search (lookup-on-search "material")}})

    "region-tag"
    (array-map
      :region-tag/id id-opts
      :region-tag/slug slug-opts
      :region-tag/name {:default ""
                        :spec types/NonBlankString
                        :input {:type :string}})

    "region"
    (array-map
      :region/id id-opts
      :region/slug slug-opts
      :region/name {:default ""
                    :spec types/NonBlankString
                    :input {:type :string}}
      ;; optional:
      :region/geojson {:default nil
                       :spec types/GeoJson
                       :input {:type :geojson
                               :simplify (fn [geojson callback]
                                           #?(:cljs (dispatch! [:state.edit/simplify! geojson callback])))
                               :get-shape (fn [query callback]
                                            #?(:cljs
                                               (dispatch! [:state.edit/get-shape! query callback])))}}
      ;; related:
      :region/region-tag-ids (tag-ids-opts-for "region"))

    "user"
    (array-map
      :user/id id-opts
      :user/name {:default ""
                  :spec types/NonBlankString
                  :input {:type :string}}
      :user/email {:default ""
                   :spec types/Email
                   :input {:type :email
                           :disabled true}}
      ;; optional:
      :user/avatar {:optional true
                    :default ""
                    :spec [:maybe types/Url]
                    :input {:type :url
                            :disabled true}})

    "photo"
    (array-map
      :photo/id id-opts
      :photo/captured-at {:default nil
                          :spec inst?
                          :input {:type :datetime}}
      :photo/colors {:default []
                     :spec [:sequential types/Color]
                     :input {:type :string
                             :disabled true}}
      :photo/width {:default nil
                    :spec integer?
                    :input {:type :integer
                            :disabled true}}
      :photo/height {:default nil
                     :spec integer?
                     :input {:type :integer
                             :disabled true}}
      ;; related
      :photo/user-id {:default nil
                      :spec uuid?
                      :input {:type :single-lookup
                              :on-find (lookup-on-find "user")
                              :on-search (lookup-on-search "user")}}
      :photo/sculpture-id {:default nil
                           :spec [:maybe uuid?]
                           :input {:type :single-lookup
                                   :on-find (lookup-on-find "sculpture")
                                   :on-search (lookup-on-search "sculpture")}})))

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

(def Entity
  (into [:multi {:dispatch (fn [e]
                             (namespace (first (keys e))))}]
        (->> schema
             keys
             (map (fn [k]
                    [k (->malli-spec k)])))))

(def SpecFor
  (memoize ->malli-spec))

