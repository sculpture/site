(ns sculpture.admin.fields.location
  (:require
    [reagent.core :as r]
    [releaflet.map :refer [map-view]]
    [humandb.ui.fields.core :refer [field]]))

; TODO improve these using: http://williams.best.vwh.net/avform.htm#LL

(defn coord-round
  "Round to 6 digits, which is 0.1m precision"
  [x]
  (/ (js/Math.round (* x 1e6)) 1e6))

(defn m->deg-lat [metres lat]
  (/ metres
     111111.0))

(defn m->deg-long [metres lat]
  (/ metres
     111111.0
     (js/Math.cos lat)))

(defmethod field :location
  [_]
  (let [search-query (r/atom "")
        ; creating on-edit here, so that the same fn is always passed to map-view below
        ; otherwise, it would keep re-setting
        on-change-fn (r/atom (fn []))
        on-edit (fn [o]
                  (let [latlng (.getLatLng o)]
                    (@on-change-fn
                      {:longitude (coord-round (.-lng latlng))
                       :latitude (coord-round (.-lat latlng))
                       :precision (js/Math.floor (.getRadius o))})))]
    (fn [{:keys [value on-change geocode]}]
      (let [pad 1.5
            location (or value {:longitude 0
                                :latitude 0
                                :precision 50})]
        [:div.field.location
         {:ref (fn [node]
                 (when node
                   (reset! on-change-fn on-change)))}
         [:form {:on-submit (fn [e]
                              (.preventDefault e)
                              (geocode @search-query (fn [{:keys [longitude latitude]}]
                                                       (on-change {:longitude (coord-round longitude)
                                                                   :latitude (coord-round latitude)
                                                                   :precision 10}))))}
          [:input {:type "text"
                   :value @search-query
                   :on-change (fn [e]
                                (reset! search-query (.. e -target -value)))}]
          [:button {} "Search"]]


         [map-view {:width "500px"
                    :height "300px"
                    :center location
                    :zoom-controls true
                    :zoom-level 16
                    :shapes [{:type :circle
                              :location {:latitude (location :latitude)
                                         :longitude (location :longitude)}
                              :radius (location :precision)
                              :editable? true
                              :bound? false
                              :on-edit on-edit}]}]

         [:table
          [:tbody
           [:tr
            [:td "Longitude:"]
            [:td
             [:input {:value (location :longitude)
                      :type "number"
                      :step 0.01
                      :on-change (fn [e]
                                   (on-change
                                     (assoc location
                                       :longitude
                                       (coord-round (js/parseFloat (.. e -target -value))))))}]
]]
           [:tr
            [:td "Latitude:"]
            [:td
             [:input {:value (location :latitude)
                      :type "number"
                      :step 0.01
                      :on-change (fn [e]
                                   (on-change
                                     (assoc location
                                       :latitude
                                       (coord-round (js/parseFloat (.. e -target -value))))))}]]]
           [:tr
            [:td "Precision:"]
            [:td
             [:input {:value (location :precision)
                      :type "integer"
                      :on-change (fn [e]
                                   (on-change
                                     (assoc location
                                       :precision
                                       (js/parseFloat (.. e -target -value)))))}]]]]]]))))
