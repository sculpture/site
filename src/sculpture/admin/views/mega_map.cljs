(ns sculpture.admin.views.mega-map
  (:require
    [releaflet.map :as leaflet]
    [sculpture.admin.state.core :refer [dispatch!]]
    [sculpture.admin.routes :as routes]
    [sculpture.admin.router :as router]
    [sculpture.admin.state.core :refer [subscribe]]))

(defn fudge [n]
  (+ n
     (- (* 0.001 (rand)) 0.0005)))

(defn mega-map-view []
  (let [config @(subscribe [:sculpture.mega-map/config])
        sculptures @(subscribe [:sculpture.mega-map/sculptures])
        sculpture-markers (->> sculptures
                               (map (fn [sculpture]
                                      (when (sculpture :location)
                                        {:location {:longitude (fudge (:longitude (sculpture :location)))
                                                    :latitude (fudge (:latitude (sculpture :location)))}
                                         :type :icon
                                         :popup (sculpture :title)
                                         :on-click (fn []
                                                     (router/go-to! (routes/entity-path {:id (sculpture :id)})))})))
                               (remove nil?))]
    [:div.mega-map
     [leaflet/map-view
      (merge
        {:width "100%"
         :height "100%"
         :zoom-level 3
         :center {:latitude 51
                  :longitude -37.8}
         :on-view-change (fn []
                           (dispatch! [:sculpture.mega-map/mark-as-dirty]))
         :shapes (if (config :markers)
                   (concat sculpture-markers (config :markers))
                   sculpture-markers)}
        config)]]))
