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
        leaflet-on-click (atom nil)
        leaflet-on-view-change (atom nil)
        create-shape-layer! (fn []
                              (when @leaflet-shape-layer
                                (.removeLayer @leaflet-map @leaflet-shape-layer))
                              (reset! leaflet-shape-layer (js/L.featureGroup))
                              (.. @leaflet-shape-layer (addTo @leaflet-map)))
        disable-interaction! (fn []
                               (.. @leaflet-map -dragging disable)
                               (.. @leaflet-map -touchZoom disable)
                               (.. @leaflet-map -doubleClickZoom disable)
                               (.. @leaflet-map -scrollWheelZoom disable)
                               (.. @leaflet-map -boxZoom disable)
                               (.. @leaflet-map -keyboard disable)
                               (when (.-tap @leaflet-map)
                                 (.. @leaflet-map -tap disable)))
        enable-interaction! (fn []
                              (.. @leaflet-map -dragging enable)
                              (.. @leaflet-map -touchZoom enable)
                              (.. @leaflet-map -doubleClickZoom enable)
                              (.. @leaflet-map -scrollWheelZoom enable)
                              (.. @leaflet-map -boxZoom enable)
                              (.. @leaflet-map -keyboard enable)
                              (when (.-tap @leaflet-map)
                                (.. @leaflet-map -tap enable)))

        create-map! (fn [node config]
                      (reset! leaflet-map (js/L.map node
                                                    (clj->js {:center (lnglat->jsloc (or (config :center)
                                                                                         {:longitude 0
                                                                                          :latitude 0}))
                                                              :zoom (get config :zoom-level 13)
                                                              :zoomControl (config :zoom-controls)
                                                              :attributionControl false})))
                      (.. (js/L.tileLayer
                            (mapbox/tilelayer-url)
                            (clj->js {:maxZoom 18}))
                          (addTo @leaflet-map))

                      (.on @leaflet-map
                           "zoomend moveend"
                           (fn [e]
                             (when @leaflet-on-view-change
                               (@leaflet-on-view-change e))))

                      (.on @leaflet-map
                           "click"
                           (fn [e]
                             (when @leaflet-on-click
                               (@leaflet-on-click e)))))

        update-map! (fn [{:keys [center bounds shapes disable-interaction?
                                 on-click on-view-change zoom-level] :as config}]

                      (when zoom-level
                        (.. @leaflet-map (setZoom zoom-level #js {:animate true})))

                      (if disable-interaction?
                        (disable-interaction!)
                        (enable-interaction!))

                      (reset! leaflet-on-click on-click)
                      (reset! leaflet-on-view-change on-view-change)

                      (when center
                        (.. @leaflet-map (panTo (lnglat->jsloc center) #js {:animate true})))

                      (when bounds
                        (.. @leaflet-map (fitBounds (clj->js [[(bounds :south) (bounds :west)]
                                                              [(bounds :north) (bounds :east)]]))))

                      (create-shape-layer!)
                      (doseq [shape-opts shapes]
                        (let [shape (make-shape shape-opts)]
                          (.. shape (addTo @leaflet-shape-layer))
                          ; markers must be on map before editing is enabled
                          (when (shape-opts :editable?)
                            (.. shape -editing enable))

                          (when (shape-opts :bound?)
                            (when-let [bounds (cond
                                                (.-getBounds shape)
                                                (.pad (.getBounds shape) 0.1)
                                                (.-getLatLng shape)
                                                (.toBounds (.getLatLng shape) 100))]
                              (.. @leaflet-map (fitBounds bounds)))))))]

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
