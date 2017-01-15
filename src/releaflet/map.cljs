(ns releaflet.map
  (:require
    [reagent.core :as r]
    [releaflet.mapbox :as mapbox]
    [releaflet.shape :refer [make-shape]]
    [releaflet.helpers :refer [lnglat->jsloc]]))

(defn map-view
  [_]
  (let [leaflet-map (atom nil)
        leaflet-shape-layer (atom nil)
        create-shape-layer! (fn []
                               (when @leaflet-shape-layer
                                 (.removeLayer @leaflet-map @leaflet-shape-layer))
                               (reset! leaflet-shape-layer (js/L.featureGroup))
                               (.. @leaflet-shape-layer (addTo @leaflet-map)))
        create-map! (fn [node config]
                      (reset! leaflet-map (js/L.map node
                                                    (clj->js {:center (lnglat->jsloc (or (config :center)
                                                                                         {:longitude 0
                                                                                          :latitude 0}))
                                                              :zoom (get config :initial-zoom-level 13)
                                                              :zoomControl (config :zoom-controls)
                                                              :attributionControl false})))
                      (.. (js/L.tileLayer
                            mapbox/tilelayer-url
                            (clj->js {:maxZoom 18}))
                          (addTo @leaflet-map)))

        update-map! (fn [{:keys [center bounds shapes] :as config}]
                      (when center
                        (.. @leaflet-map (panTo (lnglat->jsloc center))))

                      (when bounds
                        (.. @leaflet-map (fitBounds (clj->js [[(bounds :south) (bounds :west)]
                                                              [(bounds :north) (bounds :east)]]))))

                      (create-shape-layer!)
                      (doseq [shape-opts shapes]
                        (let [shape (make-shape shape-opts)]
                          (.. shape (addTo @leaflet-shape-layer))
                          (when (shape-opts :bound?)
                            (.. @leaflet-map (fitBounds (.pad (.getBounds shape) 0.1)))))))]

    (r/create-class
      {:display-name "leaflet-map"
       :component-did-mount
       (fn [this]
         (create-map! (r/dom-node this) (r/props this))
         (update-map! (r/props this)))
       :component-did-update
       (fn [this]
         (update-map! (r/props this)))
       :reagent-render
       (fn [config]
         [:div.map {:style {:height (or (config :height) "200px")
                            :width (or (config :width) "200px")}}])})))
