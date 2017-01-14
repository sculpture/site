(ns sculpture.admin.views.entity.region
  (:require
    [sculpture.admin.views.entity :refer [entity-view]]
    [sculpture.admin.views.object :refer [object-view]]
    [sculpture.admin.views.entity.partials.map :refer [map-view]]))

(defn average [& args]
  (/ (reduce + args)
     (count args)))

(defn polygon->geojson [polygon]
  (clj->js {:type "Polygon"
            :coordinates polygon}))

(defn polygon->bounds [polygon]
  (let [lngs (->> polygon
                  first
                  (map first))
        lats (->> polygon
                  first
                  (map last))]
  {:north (apply max lats)
   :south (apply min lats)
   :east (apply max lngs)
   :west (apply min lngs)}))

(defn polygon->center [polygon]
  (let [bounds (polygon->bounds polygon)]
    {:latitude (average (bounds :north) (bounds :south))
     :longitude (average (bounds :east) (bounds :west))}))

(defmethod entity-view "region"
  [region]
  [:div.region
   [:h1 (region :name)]

   (when (region :polygon)
     [map-view {:center (polygon->center (region :polygon))
                :bounds (polygon->bounds (region :polygon))
                :geojson (polygon->geojson (region :polygon))}])
   [object-view region]])
