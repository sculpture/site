(ns sculpture.geo
  #_(:require
      #(:clj [clojure.data.json :as json])))

(def wgs84-radius 6378137.0)

(def pi
  #?(:clj Math/PI
     :cljs js/Math.PI))

(defn sin [x]
  #?(:clj (Math/sin x)
     :cljs (js/Math.sin x)))

(defn abs [x]
  (if (> x 0)
    x
    (* x -1)))

(defn rad [r]
  (/ (* r pi) 180))

(defn ring-area
  "Returns area of poylgon reprsented by points [[lng lat] ...]
   Based on https://github.com/mapbox/geojson-area/blob/master/index.js"
  [coords]
  (when (> (count coords) 2)
    (->> coords
         (partition 3 1 (concat coords (take 2 coords)))
         (map (fn [[p1 p2 p3]]
                (* (- (rad (first p3))
                      (rad (first p1)))
                   (sin (rad (last p2))))))
         (reduce +)
         (* wgs84-radius wgs84-radius 0.5)
         abs)))

(defn json-parse [s]
  #?( ;:clj (json/read-str s :key-fn keyword)
     :cljs (-> (js/JSON.parse s)
              (js->clj :keywordize-keys true))))

(defn geojson->coords [geojson]
  (-> geojson
      json-parse
      :coordinates))

(defn bounding-box
  "Given points [[lng lat] ...] returns map with keys: :min-lng :max-lng :min-lat :max-lat"
  [points]
  (reduce
    (fn [memo [lng lat]]
      {:min-lng (if (< lng (memo :min-lng)) lng (memo :min-lng))
       :min-lat (if (< lat (memo :min-lat)) lat (memo :min-lat))
       :max-lng (if (< (memo :max-lng) lng) lng (memo :max-lng))
       :max-lat (if (< (memo :max-lat) lat) lat (memo :max-lat))})
    {:min-lng 180
     :min-lat 90
     :max-lng -180
     :max-lat -90}
    points))

(defn bounding-area [points]
  "Returns area of bounding-box of points [[lng lat] ...]
  Appx 15x faster than calculating the exact area"
  (let [{:keys [min-lng min-lat max-lng max-lat]} (bounding-box points)]
    (abs (/ (* (- (sin max-lat) (sin min-lat))
               (- max-lng min-lng)
               wgs84-radius
               wgs84-radius
               pi)
            180))))

(defn point-in-bounds?
  [[lng lat] points]
  (let [{:keys [min-lng min-lat max-lng max-lat]} (bounding-box points)]
    (and (< min-lng lng max-lng) (< min-lat lat max-lat))))

(defn polygon-contains?
  "Is given point [lng lat] inside of polygon represented by points [[lng lat] ...],
   Based on https://github.com/Factual/geo/blob/master/src/geo/poly.clj"
  [[lng lat] points]
  (when (and lng lat (> (count points) 3))
    (when (point-in-bounds? [lng lat] points)
      (loop [[[plng plat] & others] points
             [qlng qlat] (last points)
             inside? false]
        (let [crosses? (not= (> plat lat) (> qlat lat))
              intersects? (and crosses?
                            (< lng (+ plng (* (- qlng plng) (/ (- lat plat) (- qlat plat))))))
              inside?' (if intersects? (not inside?) inside?)]
          (if (empty? others)
            inside?'
            (recur others [plng plat] inside?')))))))
