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
     [map-view {:width "300px"
                :height "500px"
                :shapes [{:type :geojson
                          :geojson (js/JSON.parse geojson)
                          :editable? true
                          :bound? true
                          :on-edit (fn [geojson]
                                     (-> (js->clj geojson :keywordize-keys true)
                                         :geometry
                                         clj->js
                                         js/JSON.stringify
                                         on-change))}]
                :zoom-controls true}]]))

