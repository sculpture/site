(ns sculpture.admin.views.map
  (:require
    [releaflet.map :as leaflet]
    [sculpture.admin.routes :as routes]
    [sculpture.admin.state.core :refer [subscribe]]))

(defn map-view []
  (let [sculptures @(subscribe [:sculptures])]
    [:div.mega-map
     [leaflet/map-view
      {:width "100%"
       :height "100%"
       :initial-zoom-level 1
       :markers (->> sculptures
                    (map (fn [sculpture]
                           (when (sculpture :location)
                             {:location (sculpture :location)
                              :type :icon
                              :popup (sculpture :title)
                              :on-click (fn []
                                          (routes/go-to (routes/entity-path {:id (sculpture :id)})))})))
                    (remove nil?))}]]))
