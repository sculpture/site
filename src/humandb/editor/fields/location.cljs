(ns humandb.editor.fields.location
  (:require
    [reagent.core :as r]
    [releaflet.map :refer [map-view]]
    [humandb.editor.fields.core :refer [field]]))

; TODO improve these using: http://williams.best.vwh.net/avform.htm#LL

(defn tee [x]
  (println x) x)

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
                            :precision 50})
        on-map-change (fn [_ m]
                        (let [center (.getCenter m)
                              bounds (.getBounds m)]
                          (on-change
                            {:longitude (.-lng center)
                             :latitude (.-lat center)
                             :precision (location :precision)})))]
    [:div
     [map-view {:center location
                :zoom-controls true
                :initial-zoom-level 17
                :markers [{:type :circle
                           :location {:latitude (location :latitude)
                                      :longitude (location :longitude)}
                           :radius (location :precision)}]
                :on-move-end on-map-change
                :on-zoom-end on-map-change}]
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
