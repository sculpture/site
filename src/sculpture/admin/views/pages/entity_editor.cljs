(ns sculpture.admin.views.pages.entity-editor
  (:require
    [clojure.string :as string]
    [humandb.ui.field :refer [field]]
    [sculpture.admin.state.core :refer [subscribe dispatch!]]
    [sculpture.admin.cdn :as cdn]
    [sculpture.admin.routes :as routes]))

(defn lookup-on-search
  [type]
  (fn [query callback]
    (callback @(subscribe [:sculpture.edit/related-entity-search type query]))))

(defn lookup-on-find
  []
  (fn [id callback]
    (callback @(subscribe [:get-entity id]))))

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
      :city-id nil
      :location nil
      :note ""
      :tag-ids []
      :slug ""
      :date nil
      :date-precision ""
      :size nil
      :link-wikipedia "")

    "artist"
    (array-map
      :id ""
      :type "artist"
      :name ""
      :gender nil
      :nationality nil
      :link-website ""
      :link-wikipedia ""
      :birth-date nil
      :birth-date-precision nil
      :death-date nil
      :death-date-precision nil
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

    "city"
    (array-map
      :id ""
      :type "city"
      :city ""
      :region ""
      :country ""
      :slug "")

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
      :user-id nil
      :colors []
      :width nil
      :height nil)

    "user"
    (array-map
      :id ""
      :type "user"
      :name ""
      :email "")

    {:type nil}))

(defn key->title [k]
  (-> k
      name
      (string/split #"-")
      (->> (map string/capitalize)
           (string/join " "))
      (string/replace #"Id" "ID")))

(def schema
  {:id {:type :string
        :disabled true}
   :type {:type :enum
          :options #{"" "material"
                     "artist" "artist-tag"
                     "sculpture" "sculpture-tag"
                     "region" "region-tag"
                     "photo" "user" "city"}
          :disabled true}
   :name {:type :string}
   :title {:type :string}
   :email {:type :email}
   :bio {:type :string
         :length :long}
   :nationality {:type :string}
   :birth-date {:type :date}
   :birth-date-precision {:type :enum
                          :options #{""
                                     "year"
                                     "year-month"
                                     "year-month-day"}}
   :captured-at {:type :datetime}
   :death-date {:type :date}
   :death-date-precision {:type :integer}
   :slug {:type :string}
   :geojson {:type :geojson
             :simplify (fn [geojson callback]
                         (dispatch! [:sculpture.edit/simplify geojson callback]))
             :get-shape (fn [query callback]
                          (dispatch! [:sculpture.edit/get-shape query callback]))}
   :city-id {:type :single-lookup
             :on-find (lookup-on-find)
             :on-search (lookup-on-search "city")}
   :city {:type :string}
   :region {:type :string}
   :country {:type :string}
   :link-website {:type :url}
   :link-wikipedia {:type :url}
   :size {:type :integer}
   :width {:type :integer
           :disabled true}
   :height {:type :integer
            :disabled true}
   :gender {:type :enum
            :options #{"" "male" "female" "other"}}
   :note {:type :string
          :length :long}
   :date {:type :date}
   :date-precision {:type :enum
                    :options #{"" "year" "year-month" "year-month-day"}}
   :commissioned-by {:type :string}
   :location {:type :location
              :geocode (fn [query callback]
                         (dispatch! [:sculpture.edit/geocode query callback]))}
   :user-id {:type :single-lookup
             :on-find (lookup-on-find)
             :on-search (lookup-on-search "user")}
   :sculpture-id {:type :single-lookup
                  :on-find (lookup-on-find)
                  :on-search (lookup-on-search "sculpture")}
   :material-ids {:type :multi-lookup
                  :on-find (lookup-on-find)
                  :on-search (lookup-on-search "material")}
   :artist-ids {:type :multi-lookup
                :on-find (lookup-on-find)
                :on-search (lookup-on-search "artist")}
   :tag-ids {:type :multi-lookup
             :on-find (lookup-on-find)
             :on-search (lookup-on-search "sculpture-tag")}
   })

(defn field-opts [field type]
  (get (merge schema
              {:tag-ids {:type :multi-lookup
                         :on-find (lookup-on-find)
                         :on-search (lookup-on-search
                                      (case type
                                        "photo" "photo-tag"
                                        "sculpture" "sculpture-tag"
                                        "region" "region-tag"
                                        "artist" "artist-tag"
                                        nil))}})
    field
    {}))

(defn entity-editor-view [entity]
  (let [invalid-fields @(subscribe [:sculpture.edit/invalid-fields])
        default-entity (entity-defaults (:type entity))]
    [:div.entity.edit
     [:div.header
      [:h1 "Editing " (or (entity :name)
                          (entity :title)
                          (entity :slug)
                          (entity :id))]
      [:button.close
       {:on-click (fn [_]
                    (dispatch! [:sculpture.edit/stop-editing]))} "Close"]

      (let [saving? @(subscribe [:sculpture.edit/saving?])
            invalid? (not (empty? invalid-fields))]
        [:button.save {:disabled (or saving? invalid?)
                       :class (when invalid? "invalid")
                       :on-click
                       (fn [_]
                         (dispatch! [:sculpture.edit/save]))}
         (cond
           invalid?
           "Invalid"
           saving?
           "Saving..."
           :else
           "Save")])]

     [:table
      [:tbody
       (for [k (keys default-entity)]
         (let [v (or (entity k)
                     (default-entity k))]
           ^{:key k}
           [:tr {:class (when (contains? invalid-fields k)
                          "invalid")}

            [:td.key (key->title k)]
            [:td
             [field (merge
                      (field-opts k (entity :type))
                      {:value v
                       :on-change (fn [v]
                                    (dispatch! [:sculpture.edit/update-draft k v]))})]]
            [:td [:button.delete
                  {:on-click (fn []
                               (dispatch! [:sculpture.edit/remove-draft-key k]))}]]]))

       (when (= "photo" (:type entity))
         [:tr
          [:td]
          [:td
           [:img {:src (cdn/image-url entity :thumb)}]]
          [:td]])]]]))
