(ns releaflet.mapbox
  (:require
    [sculpture.admin.env :refer [env]]))

(defn token []
  (env :mapbox-token))

(defn tilelayer-url []
  (str "https://api.mapbox.com/styles/v1/mapbox/light-v9/tiles/256/{z}/{x}/{y}?access_token=" (token)))

(defn maki-marker []
  {:iconUrl
   (str "https://api.mapbox.com/v4/marker/" "pin-m-circle+00FFFF.png?access_token=" (token))})
