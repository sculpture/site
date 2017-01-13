(ns sculpture.admin.views.entity.partials.map
  (:require
    [reagent.core :as r]))

(def mapbox-token "CHANGE-ME")

(def mapbox-tilelayer (str "https://api.mapbox.com/styles/v1/mapbox/light-v9/tiles/256/{z}/{x}/{y}?access_token=" mapbox-token))

(defn lnglat->jsloc [lnglat]
  (clj->js [(lnglat :latitude)
            (lnglat :longitude)]))

(defn map-view
  [_]
  (let [leaflet-map (atom nil)
        leaflet-marker-layer (atom nil)
        create-marker-layer! (fn []
                               (when @leaflet-marker-layer
                                 (.removeLayer @leaflet-map @leaflet-marker-layer))
                               (reset! leaflet-marker-layer (js/L.layerGroup))
                               (.. @leaflet-marker-layer (addTo @leaflet-map)))
        create-map! (fn [node {:keys [center bounds marker object] :as config}]
                      (reset! leaflet-map (js/L.map node
                                                    (clj->js {:center (lnglat->jsloc (config :center))
                                                              :zoom 13
                                                              :zoomControl false
                                                              :attributionControl false })))
                      (.. (js/L.tileLayer
                            mapbox-tilelayer
                            (clj->js {:attribution "Sculpture"
                                      :maxZoom 18}))
                          (addTo @leaflet-map)))
        update-map! (fn [{:keys [center bounds marker object] :as config}]
                      (create-marker-layer!)

                      (when center
                        (.. @leaflet-map (panTo (lnglat->jsloc center))))

                      (when bounds
                        (.. @leaflet-map (fitBounds (clj->js [[(bounds :south) (bounds :west)]
                                                              [(bounds :north) (bounds :east)]]))))

                      (when marker
                        (.. (js/L.marker (lnglat->jsloc (marker :location)))
                            (addTo @leaflet-marker-layer)))

                      (when object
                        (.. (js/L.geoJSON object)
                            (addTo @leaflet-marker-layer))))]
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
       (fn []
         [:div.map {:style {:height "200px"
                            :width "200px"}}])})))
