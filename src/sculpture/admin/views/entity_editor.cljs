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

(defn expand-entity [entity]
  (let [defaults (case (:type entity)
                   "sculpture"
                   {:title ""
                    :artist-ids []
                    :commissioned-by ""
                    :material-ids []
                    :location nil
                    :note ""
                    :tag-ids []
                    :slug ""
                    :year nil
}
                   "artist"
                   {:name ""
                    :gender nil
                    :link-website ""
                    :link-wikipedia ""
                    :slug ""
                    :tag-ids []}
                   "region"
                   {:name ""
                    :geojson nil
                    :slug ""
                    :tag-ids []}
                   "material"
                   {:name ""
                    :slug ""}
                   "tag"
                   {:name ""
                    :slug ""}
                   "photo"
                   {:sculpture-id nil
                    :captured-at nil
                    :url ""
                    :user-id nil}
                   "user"
                   {:name ""
                    :email ""}
                   {:type nil})]
    (merge
      defaults
      entity)))

(def field-opts
  (array-map
    :id {:type :string
         :disabled true}
    :type {:type :enum
           :options #{"" "artist" "sculpture" "region" "material" "tag" "photo" "user"}}
    :name {:type :string}
    :title {:type :string}
    :email {:type :email}
    :slug {:type :string}
    :geojson {:type :geojson}
    :link-website {:type :url}
    :link-wikipedia {:type :url}
    :gender {:type :enum
             :options #{"" "male" "female" "other"}}
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
     (for [[k v] (into (sorted-map-by key-order-comparator) (expand-entity entity))]
       ^{:key k}
       [:tr
        [:td [:button {:on-click (fn []
                                   (dispatch! [:remove-entity-key (entity :id) k]))} "X"]]
        [:td (str k)]
        [:td
         [field (merge
                  (field-opts k)
                  {:value v
                   :on-change (fn [v]
                                (dispatch! [:update-entity (entity :id) k v]))})]]])]])
