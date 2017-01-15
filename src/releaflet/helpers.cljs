(ns releaflet.helpers)

(defn lnglat->jsloc [lnglat]
  (clj->js [(lnglat :latitude)
            (lnglat :longitude)]))
