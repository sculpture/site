(ns sculpture.admin.views.sidebar.entity.partials.list
  (:require
    [clojure.string :as string]
    [sculpture.admin.helpers :as helpers]
    [sculpture.admin.state.core :refer [subscribe]]
    [sculpture.admin.routes :as routes]
    [sculpture.admin.views.sidebar.entity.partials.photos :refer [photo-view]]))

(defmulti entity-row-data :type)

(defmethod entity-row-data "city"
  [city]
  {:title (city :city)
   :subtitle (str (city :region) ", " (city :country))
   :id (city :id)
   :photo-id nil
   :type "city"})

(defmethod entity-row-data "photo"
  [photo]
  {:title "Photo"
   :subtitle (helpers/format-date (photo :captured-at) "yyyy-MM-dd")
   :id (photo :id)
   :photo-id (:id photo)
   :type "photo"})

(defmethod entity-row-data "sculpture"
  [sculpture]
  (let [photos (subscribe [:photos-for-sculpture (sculpture :id)])
        artists (subscribe [:get-entities (sculpture :artist-ids)])]
    {:title (sculpture :title)
     :subtitle (string/join ", " (map :name @artists))
     :id (sculpture :id)
     :photo-id (:id (first @photos))
     :type "sculpture"}))

(defmethod entity-row-data :default
  [entity]
  {:title (or (entity :title) (entity :name))
   :subtitle "..."
   :id (entity :id)
   :photo-id nil
   :type (entity :type)})

(defn row-view
  [{:keys [id photo-id type title subtitle]}]
  [:a.entity
   {:href (routes/entity-path {:id id})
    :class type}
   [photo-view {:photo {:id photo-id}
                :size :thumb}]
   [:div.h1 title]
   [:div.h2 subtitle]])

(defn entity-row-view [entity]
  (let [data (entity-row-data entity)]
    [row-view data]))

(defn entity-list-view [entities]
  [:div.entity-list
   (for [entity entities]
     ^{:key (entity :id)}
     [entity-row-view entity])])
