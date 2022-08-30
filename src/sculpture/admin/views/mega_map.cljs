(ns sculpture.admin.views.mega-map
  (:require
    [bloom.commons.pages :as pages]
    [releaflet.map :as leaflet]
    [sculpture.admin.state.api :refer [subscribe dispatch!]]))

(defn fudge [n]
  (+ n
     (- (* 0.0001 (rand)) 0.00005)))

(defn mega-map-view []
  (let [config @(subscribe [:state.mega-map/config])
        sculpture-markers (->> (:sculptures config)
                               (map (fn [sculpture]
                                      (when (:sculpture/location sculpture)
                                        {:location {:longitude (fudge (:longitude (:sculpture/location sculpture)))
                                                    :latitude (fudge (:latitude (:sculpture/location sculpture)))}
                                         :type :icon
                                         :popup (:sculpture/title sculpture)
                                         :on-click (fn []
                                                     (pages/navigate-to! [:page/sculpture {:id (:sculpture/id sculpture)}]))})))
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
                           (dispatch! [:state.mega-map/mark-as-dirty!]))
         :shapes (if (config :markers)
                   (concat sculpture-markers (config :markers))
                   sculpture-markers)}
        config)]]))
