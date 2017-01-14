(ns releaflet.map
  (:require
    [reagent.core :as r]))

(def mapbox-token "pk.eyJ1IjoiYmxvb212ZW50dXJlcyIsImEiOiJjaXh3MjFtM3UwMDFkMzJvOTgxY3RpeWh4In0.d5osuJHfSInVbLrLOhcLjA")

(def mapbox-tilelayer (str "https://api.mapbox.com/styles/v1/mapbox/light-v9/tiles/256/{z}/{x}/{y}?access_token=" mapbox-token))

#_(defn create-icon-marker [location]
  (let [icon (js/L.icon (cljs->js {:iconUrl ""
                                   :shadowUrl ""
                                   :iconSize [w h]
                                   :shadowSize [w h]
                                   :iconAnchor [x y]
                                   :shadowAnchor [x y]
                                   :popupAnchor [x y]
                                   }))]
    (js/L.marker location {:icon icon})))

(defn lnglat->jsloc [lnglat]
  (clj->js [(lnglat :latitude)
            (lnglat :longitude)]))

(defn make-marker [m]
  (case (m :type)
    :circle (js/L.circle (lnglat->jsloc (m :location))
                         (clj->js
                           {:radius (m :radius)}))
    ; default
    (js/L.marker (lnglat->jsloc (m :location)))))

(defn debounce
  "Returns a debounced version of f"
  [f wait]
  (let [timeout (r/atom nil)]
    (fn [& args]
      (js/clearTimeout @timeout)
      (reset! timeout (js/setTimeout (fn []
                                       (apply f args)) wait)))))

(defn map-view
  [_]
  (let [leaflet-map (atom nil)
        leaflet-marker-layer (atom nil)
        leaflet-geojson-layer (atom nil)
        leaflet-draw-layer (atom nil)
        on-edit (atom nil)
        create-marker-layer! (fn []
                               (when @leaflet-marker-layer
                                 (.removeLayer @leaflet-map @leaflet-marker-layer))
                               (reset! leaflet-marker-layer (js/L.featureGroup))
                               (.. @leaflet-marker-layer (addTo @leaflet-map)))
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
                          (addTo @leaflet-map))

                      (.on @leaflet-map "draw:editresize"
                           (debounce (fn [e]
                                       (when @on-edit
                                         (@on-edit (.-layer e))))
                                     250))

                      (.on @leaflet-map "draw:editvertex"
                           (fn [e]
                             (when @on-edit
                               (@on-edit (.toGeoJSON @leaflet-geojson-layer))))))
        update-map! (fn [{:keys [center bounds markers geojson zoom-level draw?] :as config}]
                      (create-marker-layer!)

                      (reset! on-edit (config :on-edit))

                      (when center
                        (.. @leaflet-map (panTo (lnglat->jsloc center))))

                      (when bounds
                        (.. @leaflet-map (fitBounds (clj->js [[(bounds :south) (bounds :west)]
                                                              [(bounds :north) (bounds :east)]]))))

                      (doseq [marker markers]
                        (let [m (make-marker marker)]
                          (.. m (addTo @leaflet-marker-layer))
                          (when (marker :editable?)
                            (.enable (js/L.EditToolbar.Edit.
                                       @leaflet-map
                                       (clj->js {:featureGroup @leaflet-marker-layer
                                                 :remove false})))

                            (when (marker :on-drag-end)
                              (.on m "dragend"
                                   (fn [e]
                                     ((marker :on-drag-end) (.-target e))))))

                          (when (marker :bound?)
                            (.. @leaflet-map (fitBounds (.pad (.getBounds m)
                                                              0.1))))))

                      (when @leaflet-geojson-layer
                        (.removeLayer @leaflet-map @leaflet-geojson-layer))
                      (when geojson
                        (reset! leaflet-geojson-layer (js/L.geoJSON geojson))
                        (.. @leaflet-geojson-layer (addTo @leaflet-map))
                        (.fitBounds @leaflet-map (.getBounds @leaflet-geojson-layer)))


                      (when draw?
                        (.enable (js/L.EditToolbar.Edit.
                                  @leaflet-map
                                  (clj->js {:featureGroup @leaflet-geojson-layer
                                            :remove false})))))]

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
