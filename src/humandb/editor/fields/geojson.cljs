(ns humandb.editor.fields.geojson
  (:require
    [reagent.core :as r]
    [humandb.editor.fields.core :refer [field]]
    [releaflet.map :as leaflet]))

(defmethod field :geojson
  [_]
  (let [search-query (r/atom "")
        ; creating on-edit here, so that the same fn is always passed to map-view below
        ; otherwise, it would keep re-setting
        on-change-fn (r/atom (fn []))
        on-edit (fn [geojson]
                  (@on-change-fn
                    (->> (js->clj geojson :keywordize-keys true)
                         :geometry
                         clj->js
                         js/JSON.stringify)))]
    (fn [{:keys [value on-change get-shape]}]
      (let [geojson (or value (js/JSON.stringify (clj->js {:type "Polygon"
                                                           :coordinates [[[-10 10]
                                                                          [10 10]
                                                                          [10 -10]
                                                                          [-10 -10]]]})))]
        [:div
         {:ref (fn [node]
                 (when node
                   (reset! on-change-fn on-change)))}
         [:form {:on-submit (fn [e]
                              (.preventDefault e)
                              (get-shape @search-query
                                (fn [result]
                                  (on-change (:geojson result)))))}
          [:input {:type "text"
                   :value @search-query
                   :on-change (fn [e]
                                (reset! search-query (.. e -target -value)))}]
          [:button {} "Search"]]

         [leaflet/map-view
          {:width "500px"
           :height "500px"
           :shapes [{:type :geojson
                     :geojson (js/JSON.parse geojson)
                     :editable? true
                     :bound? true
                     :on-edit on-edit}]
           :zoom-controls true}]
         [:textarea {:value geojson
                     :on-change (fn [e]
                                  (on-change (js/JSON.stringify (js/JSON.parse (.. e -target -value)))))}]]))))

