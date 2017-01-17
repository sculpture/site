(ns sculpture.admin.views.mega-map
  (:require
    [releaflet.map :as leaflet]
    [sculpture.admin.state.core :refer [dispatch!]]
    [sculpture.admin.routes :as routes]
    [sculpture.admin.router :as router]
    [sculpture.admin.state.core :refer [subscribe]]))

(defn mega-map-view []
  (let [config @(subscribe [:sculpture.mega-map/config])
        sculptures @(subscribe [:sculptures])]
    [:div.mega-map
     [leaflet/map-view
      (merge
        {:width "100%"
         :height "100%"
         :zoom-level 1
         :on-view-change (fn []
                           (dispatch! [:sculpture.mega-map/mark-as-dirty]))
         :shapes (->> sculptures
                      (map (fn [sculpture]
                             (when (sculpture :location)
                               {:location (sculpture :location)
                                :type :icon
                                :popup (sculpture :title)
                                :on-click (fn []
                                            (router/go-to! (routes/entity-path {:id (sculpture :id)})))})))
                      (remove nil?))}
        config)]]))
