(ns sculpture.admin.views.sidebar.entity.region
  (:require
    [sculpture.admin.state.api :refer [dispatch!]]
    [sculpture.admin.views.sidebar.entity :refer [entity-handler]]
    [sculpture.admin.views.sidebar.entity.partials.related-sculptures :refer [related-sculptures-view]]
    [sculpture.admin.views.sidebar.entity.partials.map :refer [map-view]]))

(defn region-entity-view
  [region]
  [:div.region.entity
   (when (:region/geojson region)
     [map-view {:width "100%"
                :disable-interaction? true
                :on-click (fn [_]
                            (dispatch! [:state.mega-map/show! [{:type :geojson
                                                                :bound? true
                                                                :geojson (js/JSON.parse (:region/geojson region))}]]))
                :shapes [{:type :geojson
                          :bound? true
                          :geojson (js/JSON.parse (:region/geojson region))}]}])
   [:div.info
    [:h1 (:region/name region)]]
   [:div.related
    [:h2 "Sculptures"]
    [related-sculptures-view (:region/sculptures region)]]])

(defmethod entity-handler :region
  [_ region-id]
  {:identifier {:region/id region-id}
   :pattern [:region/id
             :region/name
             :region/geojson
             {:region/sculptures
              [:sculpture/id
               :sculpture/title
               {:sculpture/artists [:artist/name]}
               {:sculpture/photos [:photo/id]}]}]
   :view region-entity-view})
