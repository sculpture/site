(ns humandb.editor.fields.geojson
  (:require
    [reagent.core :as r]
    [humandb.editor.fields.core :refer [field]]
    [sculpture.admin.views.entity.partials.map :refer [map-view]]))

(defmethod field :geojson
  [{:keys [value on-change]}]
  (let [geojson (or value (js/JSON.stringify (clj->js {:type "Polygon"
                                                       :coordinates [[[-10 10]
                                                                      [10 10]
                                                                      [10 -10]
                                                                      [-10 -10]]]})))]
    [:div
     [map-view {:geojson (js/JSON.parse geojson)
                :draw? true
                :width "300px"
                :height "500px"
                :zoom-controls true
                :on-edit (fn [geojson]
                           (-> (js->clj geojson :keywordize-keys true)
                               :features
                               first
                               :geometry
                               clj->js
                               js/JSON.stringify
                               on-change))}]]))

