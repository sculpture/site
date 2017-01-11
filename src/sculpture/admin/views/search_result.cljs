(ns sculpture.admin.views.search-result
  (:require
    [clojure.string :as string]
    [re-frame.core :refer [subscribe dispatch]]
    [sculpture.admin.views.entity.partials.photos :refer [photo-view]]))

(defmulti search-result-view :type)

(defmethod search-result-view "sculpture"
  [sculpture]
  (let [photos (subscribe [:photos-for-sculpture (sculpture :id)])
        artists (subscribe [:get-entities (sculpture :artist-ids)])]
    [:div.sculpture
     (when-let [photo (first @photos)]
       [photo-view photo :thumb false])
     [:div.title (sculpture :title)]
     [:div.artist
      (string/join ", " (map :name @artists))]]))

(defmethod search-result-view :default
  [entity]
  [:div (entity :name)])
