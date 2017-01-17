(ns sculpture.admin.views.entity.region
  (:require
    [sculpture.admin.views.entity :refer [entity-view]]
    [sculpture.admin.views.object :refer [object-view]]
    [sculpture.admin.views.entity.partials.map :refer [map-view]]))

(defmethod entity-view "region"
  [region]
  [:div.region
   [:div.banner]
   [:div.info
    [:h1 (region :name)]]
   [:div.extra
    (when (region :geojson)
      [map-view {:shapes [{:type :geojson
                           :bound? true
                           :geojson (js/JSON.parse (region :geojson))}]}])]])
