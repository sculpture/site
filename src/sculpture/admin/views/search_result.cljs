(ns sculpture.admin.views.search-result
  (:require
    [clojure.string :as string]
    [sculpture.admin.state.core :refer [subscribe]]
    [sculpture.admin.routes :as routes]
    [sculpture.admin.views.entity.partials.photos :refer [photo-view]]))

(defmulti search-result-data :type)

(defmethod search-result-data "sculpture"
  [sculpture]
  (let [photos (subscribe [:photos-for-sculpture (sculpture :id)])
        artists (subscribe [:get-entities (sculpture :artist-ids)])]
    {:h1 (sculpture :title)
     :h2 (string/join ", " (map :name @artists))
     :image (first @photos)}))

(defmethod search-result-data :default
  [entity]
  {:h1 (entity :name)
   :h2 "..."
   :image nil})

(defn search-result-view [entity]
  (let [data (search-result-data entity)]
    [:a.result {:href (routes/entity-path {:id (entity :id)})}
     [photo-view {:photo (data :image)
                  :size :thumb}]
     [:div.h1 (data :h1)]
     [:div.h2 (data :h2)]]))
