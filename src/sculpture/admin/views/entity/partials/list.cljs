(ns sculpture.admin.views.entity.partials.list
  (:require
    [clojure.string :as string]
    [sculpture.admin.state.core :refer [subscribe]]
    [sculpture.admin.routes :as routes]
    [sculpture.admin.views.entity.partials.photos :refer [photo-view]]))

(defmulti entity-row-data :type)

(defmethod entity-row-data "sculpture"
  [sculpture]
  (let [photos (subscribe [:photos-for-sculpture (sculpture :id)])
        artists (subscribe [:get-entities (sculpture :artist-ids)])]
    {:h1 (sculpture :title)
     :h2 (string/join ", " (map :name @artists))
     :id (sculpture :id)
     :image (first @photos)
     :type "sculpture"}))

(defmethod entity-row-data :default
  [entity]
  {:h1 (or (entity :title) (entity :name))
   :h2 "..."
   :id (entity :id)
   :image nil
   :type (entity :type)})

(defn entity-row-view [entity]
  (let [data (entity-row-data entity)]
    [:a.entity
     {:href (routes/entity-path {:id (data :id)})
      :class (data :type)}
     [photo-view {:photo (data :image)
                  :size :thumb}]
     [:div.h1 (data :h1)]
     [:div.h2 (data :h2)]]))

(defn entity-list-view [entities]
  [:div.entity-list
   (for [entity entities]
     ^{:key (entity :id)}
     [entity-row-view entity])])
