(ns sculpture.admin.views.sidebar.entity.region
  (:require
    [sculpture.admin.state.core :refer [subscribe dispatch!]]
    [sculpture.admin.views.sidebar.entity :refer [entity-view]]
    [sculpture.admin.views.sidebar.entity.partials.related-sculptures :refer [related-sculptures-view]]
    [sculpture.admin.views.sidebar.entity.partials.map :refer [map-view]]))

(defmethod entity-view "region"
  [region]
  [:div.region.entity
   (when (region :geojson)
     [map-view {:width "100%"
                :disable-interaction? true
                :on-click (fn [_]
                            (dispatch! [:sculpture.mega-map/show [{:type :geojson
                                                                   :bound? true
                                                                   :geojson (js/JSON.parse (region :geojson))}]]))
                :shapes [{:type :geojson
                          :bound? true
                          :geojson (js/JSON.parse (region :geojson))}]}])
   [:div.info
    [:h1 (region :name)]]
   [:div.related
    [:h2 "Sculptures"]
    [related-sculptures-view @(subscribe [:get-entities (region :sculpture-ids)])]]])
