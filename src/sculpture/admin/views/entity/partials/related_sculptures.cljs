(ns sculpture.admin.views.entity.partials.related-sculptures
  (:require
    [re-frame.core :refer [subscribe dispatch]]
    [sculpture.admin.views.entity.partials.photos :refer [photo-view]]))

(defn related-sculpture-view [sculpture]
  (let [photos (subscribe [:photos-for-sculpture (sculpture :id)])]
    [:a {:href ""
         :on-click (fn [e]
                     (.preventDefault e)
                     (dispatch [:set-active-entity-id (sculpture :id)]))}
     [photo-view (first @photos) :thumb false]
     (sculpture :title)
     (sculpture :year)]))

(defn related-sculptures-view [sculptures]
  [:div
   (for [sculpture sculptures]
     ^{:key (sculpture :id)}
     [related-sculpture-view sculpture])])