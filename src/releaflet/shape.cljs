(ns releaflet.shape
  (:require
    [releaflet.helpers :refer [lnglat->jsloc]]))

(defn create-geojson-object [m]
  (let [geojson-layer-group (js/L.geoJSON (m :geojson))
        shape (aget (.getLayers geojson-layer-group) 0)]
    shape))

(defn measle [color]
  {:iconUrl (case color
              :red "https://maps.gstatic.com/intl/en_us/mapfiles/markers2/measle.png"
              :blue "https://maps.gstatic.com/intl/en_ALL/mapfiles/markers2/measle_blue.png")
   :iconSize [7 7]})

(defn create-icon-marker [m]
  (js/L.marker (lnglat->jsloc (m :location))
               #js {:icon (js/L.icon (clj->js (measle :red)))}))

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
