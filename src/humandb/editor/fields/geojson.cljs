(ns humandb.editor.fields.geojson
  (:require
    [reagent.core :as r]
    [humandb.editor.fields.core :refer [field]]
    [sculpture.admin.views.entity.partials.map :refer [map-view]]))

(defmethod field :geojson
  [{:keys [value on-change]}]
  (let [geojson (or value "")]
    [:div
     [map-view {:geojson (js/JSON.parse geojson)}]]))

