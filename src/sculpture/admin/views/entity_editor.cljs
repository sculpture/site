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

(defn entity-defaults [type]
  (case type
    "sculpture"
    (array-map
      :id ""
      :type "sculpture"
      :title ""
      :artist-ids []
      :commissioned-by ""
      :material-ids []
      :location nil
      :note ""
      :tag-ids []
      :slug ""
      :date nil
      :date-precision ""
      :size nil)

    "artist"
    (array-map
      :id ""
      :type "artist"
      :name ""
      :gender nil
      :link-website ""
      :link-wikipedia ""
      :birth-date nil
      :birth-date-accuracy nil
      :death-date nil
      :death-date-accuracy nil
      :slug ""
      :tag-ids [])

    "region"
    (array-map
      :id ""
      :type "region"
      :name ""
      :geojson nil
      :slug ""
      :tag-ids [])

    "material"
    (array-map
      :id ""
      :type "material"
      :name ""
      :slug "")

    "artist-tag"
    (array-map
      :id ""
      :type "artist-tag"
      :name ""
      :slug "")

    "sculpture-tag"
    (array-map
      :id ""
      :type "sculpture-tag"
      :name ""
      :slug "")

    "region-tag"
    (array-map
      :id ""
      :type "region-tag"
      :name ""
      :slug "")

    "photo"
    (array-map
      :id ""
      :type "photo"
      :sculpture-id nil
      :captured-at nil
      :url ""
      :user-id nil)

    "user"
    (array-map
      :id ""
      :type "user"
      :name ""
      :email "")

    {:type nil}))

(defn field-opts [field type]
  (get {:id {:type :string
             :disabled true}
        :type {:type :enum
               :options #{"" "artist" "sculpture"
                          "region" "material" "tag"
                          "photo" "user"}}
        :name {:type :string}
        :title {:type :string}
        :email {:type :email}
        :bio {:type :string
              :length :long}
        :birth-date {:type :date}
        :birth-date-accuracy {:type :integer}
        :death-date {:type :date}
        :death-date-accuracy {:type :integer}
        :slug {:type :string}
        :geojson {:type :geojson}
        :link-website {:type :url}
        :link-wikipedia {:type :url}
        :size {:type :integer}
        :gender {:type :enum
                 :options #{"" "male" "female" "other"}}
        :note {:type :string
               :length :long}
        :date {:type :date}
        :date-precision {:type :enum
                         :options #{"" "year" "year-month" "year-month-day"}}
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
                  :on-search (lookup-on-search
                               (case type
                                 "sculpture" "sculpture-tag"
                                 "region" "region-tag"
                                 "artist" "artist-tag"))}}
    field
    {}))

(defn entity-editor-view [entity]
  [:table
    [:tbody
     (let [default-entity (entity-defaults (:type entity))]
       (for [k (keys default-entity)]
         (let [v (or (entity k)
                     (default-entity k))]
           ^{:key k}
           [:tr
            [:td [:button {:on-click (fn []
                                       (dispatch! [:sculpture.edit/remove-entity-key (entity :id) k]))} "X"]]
            [:td (str k)]
            [:td
             [field (merge
                      (field-opts k (entity :type))
                      {:value v
                       :on-change (fn [v]
                                    (dispatch! [:sculpture.edit/update-entity (entity :id) k v]))})]]])))]])
