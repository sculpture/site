(ns releaflet.map
  (:require
    [reagent.core :as r]))

(def mapbox-token "pk.eyJ1IjoiYmxvb212ZW50dXJlcyIsImEiOiJjaXh3MjFtM3UwMDFkMzJvOTgxY3RpeWh4In0.d5osuJHfSInVbLrLOhcLjA")

(def mapbox-tilelayer (str "https://api.mapbox.com/styles/v1/mapbox/light-v9/tiles/256/{z}/{x}/{y}?access_token=" mapbox-token))

(defn lnglat->jsloc [lnglat]
  (clj->js [(lnglat :latitude)
            (lnglat :longitude)]))

(defn maki-marker []
  {:iconUrl
   (str "https://api.mapbox.com/v4/marker/" "pin-m-circle+00FFFF.png?access_token=" mapbox-token)})

(defn measle [color]
  {:iconUrl (case color
              :red "https://maps.gstatic.com/intl/en_us/mapfiles/markers2/measle.png"
              :blue "https://maps.gstatic.com/intl/en_ALL/mapfiles/markers2/measle_blue.png")
   :iconSize [7 7]})

(defn create-icon-marker [m]
  (js/L.marker (lnglat->jsloc (m :location))
               #js {:icon (js/L.icon (clj->js (measle :red)))}))

(defn create-geojson-object [m]
  (let [geojson-layer-group (js/L.geoJSON (m :geojson))
        shape (aget (.getLayers geojson-layer-group) 0)]
    shape))

(defn make-shape [opts]
  (let [shape (case (opts :type)
                :circle (js/L.circle (lnglat->jsloc (opts :location))
                                     (clj->js
                                       {:radius (opts :radius)}))
                :icon (create-icon-marker opts)
                :geojson (create-geojson-object opts)
                ; default
                (js/L.marker (lnglat->jsloc (opts :location))))]

    (when (opts :popup)
      (.bindPopup shape (opts :popup) #js {:closeButton false})
      (.on shape "mouseover" (fn [_] (.openPopup shape)))
      (.on shape "mouseout" (fn [_] (.closePopup shape))))

    (when (opts :on-click)
      (.on shape "click"
           (fn [e]
             ((opts :on-click) (.-target e)))))

    (when (opts :editable?)
      (.. shape -editing enable))

    (when (opts :on-edit)
      (.. shape (on "edit" (fn [e]
                             (case (opts :type)
                               :geojson
                               ((opts :on-edit) (.toGeoJSON shape))
                               ((opts :on-edit) shape))))))

    (when (opts :on-drag-end)
      (.on shape "dragend"
           (fn [e]
             ((opts :on-drag-end) (.-target e)))))

    shape))

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
                            mapbox-tilelayer
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
