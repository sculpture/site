(ns sculpture.admin.views.entity.region
  (:require
    [sculpture.admin.views.entity :refer [entity-view]]
    [sculpture.admin.views.object :refer [object-view]]
    [sculpture.admin.views.entity.partials.map :refer [map-view]]))

(defn average [& args]
  (/ (reduce + args)
     (count args)))

(defmethod entity-view "region"
  [region]
  [:div.region
   [:h1 (region :name)]

   (when (region :geojson)
     [map-view {:center {:latitude (average
                                     (get-in region [:bounds :north])
                                     (get-in region [:bounds :south]))
                         :longitude (average
                                      (get-in region [:bounds :east])
                                      (get-in region [:bounds :west]))}
                :bounds (get-in region [:bounds])
                :object (js/JSON.parse (region :geojson))}])
   [object-view region]])
