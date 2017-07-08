(ns humandb.editor.fields.location
  (:require
    [reagent.core :as r]
    [releaflet.map :refer [map-view]]
    [humandb.editor.fields.core :refer [field]]))

; TODO improve these using: http://williams.best.vwh.net/avform.htm#LL

(defn m->deg-lat [metres lat]
  (/ metres
     111111.0))

(defn m->deg-long [metres lat]
  (/ metres
     111111.0
     (js/Math.cos lat)))

(defmethod field :location
  [{:keys [value on-change]}]
  (let [pad 1.5
        location (or value {:longitude 0
                            :latitude 0
                            :precision 50})]
    [:div
     [map-view {:center location
                :zoom-controls true
                :zoom-level 17
                :shapes [{:type :circle
                          :location {:latitude (location :latitude)
                                     :longitude (location :longitude)}
                          :radius (location :precision)
                          :editable? true
                          :bound? true
                          :on-edit (fn [o]
                                     (let [latlng (.getLatLng o)]
                                       (on-change
                                         {:longitude (.-lng latlng)
                                          :latitude (.-lat latlng)
                                          :precision (.getRadius o)})))}]}]
     [:div
      "Longitude:"
      [:input {:value (location :longitude)
               :type "number"
               :step 0.01
               :on-change (fn [e]
                            (on-change
                              (assoc location
                                :longitude
                                (.. e -target -value))))}]]
     [:div
      "Latitude:"
      [:input {:value (location :latitude)
               :type "number"
               :step 0.01
               :on-change (fn [e]
                            (on-change
                              (assoc location
                                :latitude
                                (.. e -target -value))))}]]
     [:div
      "Precision:"
      [:input {:value (location :precision)
               :type "number"
               :on-change (fn [e]
                            (on-change
                              (assoc location
                                :precision
                                (.. e -target -value))))}]]]))
