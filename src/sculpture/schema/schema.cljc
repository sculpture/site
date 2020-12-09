(ns sculpture.schema.schema
  (:require
    [sculpture.schema.types :as types]
    #?@(:cljs
         [[sculpture.admin.state.core :refer [subscribe dispatch!]]])))

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

(def id-opts
  {:default nil
   :spec uuid?
   :input {:type :string
           :disabled true}})

(def slug-opts
  {:default ""
   :spec types/Slug
   :input {:type :string}})

(defn type-opts-for [entity-type]
  (let [types #{"" "artist" "artist-tag" "category"
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
   :spec types/RelatedIds
   :input {:type :multi-lookup
           :optional true
           :on-find (lookup-on-find)
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
      :id id-opts
      :type (type-opts-for "artist-tag")
      :slug slug-opts
      :name {:default ""
             :spec types/NonBlankString
             :input {:type :string}})

    "artist"
    (array-map
      :id id-opts
      :type (type-opts-for "artist")
      :slug slug-opts
      :name {:default ""
             :spec types/NonBlankString
             :input {:type :string}}
      ;; optional:
      :gender {:default nil
               :optional true
               :spec [:maybe [:enum "male" "female" "other"]]
               :input {:type :enum
                       :options #{"" "male" "female" "other"}}}
      :nationality {:default nil
                    :optional true
                    :spec [:maybe types/NonBlankString]
                    :input {:type :string}}
      :link-website {:default nil
                     :optional true
                     :spec [:maybe types/Url]
                     :input {:type :url}}
      :link-wikipedia {:default nil
                       :optional true
                       :spec [:maybe types/Url]
                       :input {:type :url}}
      :birth-date {:default nil
                   :optional true
                   :spec [:maybe types/FlexDate]
                   :input {:type :flexdate}}
      :death-date {:default nil
                   :optional true
                   :spec [:maybe types/FlexDate]
                   :input {:type :flexdate}}
      :bio {:default nil
            :optional true
            :spec [:maybe types/NonBlankString]
            :input {:type :string
                    :length :long}}
      ;; related:
      :tag-ids (tag-ids-opts-for "artist"))

    "city"
    (array-map
      :id id-opts
      :type (type-opts-for "city")
      :slug slug-opts
      :city {:default ""
             :spec types/NonBlankString
             :input {:type :string}}
      :region {:default ""
               :spec types/NonBlankString
               :input {:type :string}}
      :country {:default ""
                :spec types/NonBlankString
                :input {:type :string}})

    "material"
    (array-map
      :id id-opts
      :type (type-opts-for "material")
      :slug slug-opts
      :name {:default ""
             :spec types/NonBlankString
             :input {:type :string}})

    "category"
    (array-map
      :id id-opts
      :type (type-opts-for "category")
      :slug slug-opts
      :name {:default ""
             :spec types/NonBlankString
             :input {:type :string}})

    "sculpture-tag"
    (array-map
      :id id-opts
      :type (type-opts-for "sculpture-tag")
      :slug slug-opts
      :name {:default ""
             :spec types/NonBlankString
             :input {:type :string}}
      ;; optional:
      :category-id {:default nil
                    :optional true
                    :spec [:maybe uuid?]
                    ;; wrap in a function so sub can be called later
                    :input (fn []
                             {:type :enum-lookup
                              :options
                              #?(:cljs (->> @(subscribe [:sculpture.edit/entities-of-type "category"])
                                            (sort-by :name)
                                            (map (fn [entity]
                                                   [(:id entity) (:name entity)]))
                                            (into {}))
                                 :clj nil)})})

    "sculpture"
    (array-map
      :id id-opts
      :type (type-opts-for "sculpture")
      :slug slug-opts
      :title {:default ""
              :spec types/NonBlankString
              :input {:type :string}}
      ;; optional:
      :location {:default nil
                 :spec types/Location
                 :input {:type :location
                         :geocode (fn [query callback]
                                    #?(:cljs
                                       (dispatch! [:sculpture.edit/geocode query callback])))}}
      :note {:default nil
             :optional true
             :spec [:maybe types/NonBlankString]
             :input {:type :string
                     :length :long}}
      :commissioned-by {:optional true
                        :default ""
                        :spec [:maybe types/NonBlankString]
                        :input {:type :string}}
      :date {:optional true
             :default nil
             :spec [:maybe types/FlexDate]
             :input {:type :flexdate}}
      :size {:optional true
             :default nil
             :spec [:maybe integer?]
             :input {:type :integer}}
      :link-wikipedia {:optional true
                       :default nil
                       :spec [:maybe types/Url]
                       :input {:type :url}}
      ;; related:
      :tag-ids (tag-ids-opts-for "sculpture")
      :city-id {:default nil
                :optional true
                :spec [:maybe uuid?]
                :input {:type :single-lookup
                        :on-find (lookup-on-find)
                        :on-search (lookup-on-search "city")}}
      :artist-ids {:default []
                   :spec types/RelatedIds
                   :input {:type :multi-lookup
                           :on-find (lookup-on-find)
                           :on-search (lookup-on-search "artist")}}
      :material-ids {:default []
                     :spec types/RelatedIds
                     :input {:type :multi-lookup
                             :on-find (lookup-on-find)
                             :on-search (lookup-on-search "material")}})

    "region-tag"
    (array-map
      :id id-opts
      :type (type-opts-for "region-tag")
      :slug slug-opts
      :name {:default ""
             :spec types/NonBlankString
             :input {:type :string}})

    "region"
    (array-map
      :id id-opts
      :type (type-opts-for "region")
      :slug slug-opts
      :name {:default ""
             :spec types/NonBlankString
             :input {:type :string}}
      ;; optional:
      :geojson {:default nil
                :spec types/GeoJson
                :input {:type :geojson
                        :simplify (fn [geojson callback]
                                    #?(:cljs (dispatch! [:sculpture.edit/simplify geojson callback])))
                        :get-shape (fn [query callback]
                                     #?(:cljs
                                        (dispatch! [:sculpture.edit/get-shape query callback])))}}
      ;; related:
      :tag-ids (tag-ids-opts-for "region"))

    "user"
    (array-map
      :id id-opts
      :type (type-opts-for "user")
      :name {:default ""
             :spec types/NonBlankString
             :input {:type :string}}
      :email {:default ""
              :spec types/Email
              :input {:type :email
                      :disabled true}}
      ;; optional:
      :avatar {:optional true
               :default ""
               :spec [:maybe types/Url]
               :input {:type :url
                       :disabled true}})

    "photo"
    (array-map
      :id id-opts
      :type (type-opts-for "photo")
      :captured-at {:default nil
                    :spec inst?
                    :input {:type :datetime}}
      :colors {:default []
               :spec [:vector types/Color]
               :input {:type :string
                       :disabled true}}
      :width {:default nil
              :spec integer?
              :input {:type :integer
                      :disabled true}}
      :height {:default nil
               :spec integer?
               :input {:type :integer
                       :disabled true}}
      ;; related
      :user-id {:default nil
                :spec uuid?
                :input {:type :single-lookup
                        :on-find (lookup-on-find)
                        :on-search (lookup-on-search "user")}}
      :sculpture-id {:default nil
                     :spec [:maybe uuid?]
                     :input {:type :single-lookup
                             :on-find (lookup-on-find)
                             :on-search (lookup-on-search "sculpture")}})))

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

(defn ->malli-spec [entity-type]
  (into [:map]
        (->> (schema entity-type)
             (map (fn [[k v]]
                    [k {:optional (:optional v)} (:spec v)])))))

(def Entity
  (into [:multi {:dispatch :type}]
        (->> schema
             keys
             (map (fn [k]
                    [k (->malli-spec k)])))))
