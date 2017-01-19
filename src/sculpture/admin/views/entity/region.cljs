(ns sculpture.admin.views.entity.region
  (:require
    [sculpture.admin.state.core :refer [dispatch!]]
    [sculpture.admin.views.entity :refer [entity-view]]
    [sculpture.admin.views.object :refer [object-view]]
    [sculpture.admin.views.entity.partials.map :refer [map-view]]))

(defmethod entity-view "region"
  [region]
  [:div.region.entity
   (when (region :geojson)
     [map-view {:width "100%"
                :disable-interaction? true
                :on-click (fn [_]
                            (dispatch! [:sculpture.mega-map/show {:type :geojson
                                                                  :bound? true
                                                                  :geojson (js/JSON.parse (region :geojson))}]))
                :shapes [{:type :geojson
                          :bound? true
                          :geojson (js/JSON.parse (region :geojson))}]}])
   [:div.info
    [:h1 (region :name)]]])
