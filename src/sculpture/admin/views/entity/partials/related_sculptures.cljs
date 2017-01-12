(ns sculpture.admin.views.entity.partials.related-sculptures
  (:require
    [sculpture.admin.state.core :refer [subscribe]]
    [sculpture.admin.routes :as routes]
    [sculpture.admin.views.entity.partials.photos :refer [photo-view]]))

(defn related-sculpture-view [sculpture]
  (let [photos (subscribe [:photos-for-sculpture (sculpture :id)])]
    [:a.sculpture
     {:href (routes/entity-path {:id (sculpture :id)})}
     [photo-view (first @photos) :thumb false]
     [:div.title (sculpture :title)]
     [:div.year (sculpture :year)]]))

(defn related-sculptures-view [sculptures]
  [:div.sculptures
   (for [sculpture sculptures]
     ^{:key (sculpture :id)}
     [related-sculpture-view sculpture])])
