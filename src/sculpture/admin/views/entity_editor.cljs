(ns sculpture.admin.views.entity-editor
  (:require
    [sculpture.admin.state.core :refer [subscribe dispatch!]]
    [sculpture.admin.routes :as routes]
    [humandb.editor.field :refer [field]]))

(defn lookup-on-search
  [type]
  (fn [query callback]
    (callback @(subscribe [:related-entity-search type query]))))

(defn lookup-on-find
  []
  (fn [id callback]
    (callback @(subscribe [:get-entity id])) ))

(def field-opts
  (array-map
    :id {:type :string
         :disabled true}
    :type {:type :enum
           :options #{"artist" "sculpture" "region" "material" "tag" "photo"}}
    :name {:type :string}
    :title {:type :string}
    :email {:type :email}
    :slug {:type :string}
    :geojson {:type :geojson}
    :link-website {:type :url}
    :link-wikipedia {:type :url}
    :gender {:type :enum
             :options #{nil "male" "female" "other"}}
    :note {:type :string
           :length :long}
    :year {:type :integer}
    :commissioned-by {:type :string}
    :location {:type :location}
    :material-ids {:type :multi-lookup
                   :on-find (lookup-on-find)
                   :on-search (lookup-on-search "material")}
    :artist-ids {:type :multi-lookup
                 :on-find (lookup-on-find)
                 :on-search (lookup-on-search "artist")}
    :tag-ids {:type :multi-lookup
              :on-find (lookup-on-find)
              :on-search (lookup-on-search "tag")}))

(defn key-order-comparator [key1 key2]
  (compare (.indexOf (vec (keys field-opts)) key1)
           (.indexOf (vec (keys field-opts)) key2)))

(defn entity-editor-view [entity]
  [:table
    [:tbody
     (for [[k v] (into (sorted-map-by key-order-comparator) entity)]
       ^{:key k}
       [:tr
        [:td (str k)]
        [:td
         [field (merge
                  (field-opts k)
                  {:value v
                   :on-change (fn [v]
                                (dispatch! [:update (entity :id) k v]))})]]])]])
