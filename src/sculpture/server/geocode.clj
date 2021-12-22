(ns sculpture.server.geocode
  (:require
    [clojure.set :refer [rename-keys]]
    [org.httpkit.client :as http]
    [sculpture.config :refer [config]]
    [sculpture.json :as json]
    [sculpture.db.pg.util :as util]))

(defn mapquest-osm-search [query]
  (-> @(http/request
         {:method :get
          :url "http://open.mapquestapi.com/nominatim/v1/search.php"
          :query-params {:key (:mapquest-api-key config)
                         :format "json"
                         :q query
                         :limit 1
                         :polygon_geojson 1}})
      :body
      json/decode
      first))

(defn locationiq-osm-search [query]
  (-> @(http/request
         {:method :get
          :url "http://locationiq.org/v1/search.php"
          :query-params {:key (:location-iq-api-key config)
                         :format "json"
                         :q query
                         :limit 1
                         :polygon_geojson 1}})
      :body
      json/decode
      first
      :geojson))

(defn osm-geocode [query]
  (-> (mapquest-osm-search query)
      (select-keys [:lat :lon])
      (rename-keys {:lat :latitude :lon :longitude})))

(defn mapquest-geocode [query]
  (-> @(http/request
         {:method :get
          :url "http://open.mapquestapi.com/geocoding/v1/address"
          :query-params {:key (:mapquest-api-key config)
                         :format "json"
                         :location query
                         :maxResults 1}})
      :body
      json/decode
      :results
      first
      :locations
      first
      :displayLatLng
      (rename-keys {:lat :latitude :lng :longitude})))

(defn google-geocode [query]
  (-> @(http/request
         {:method :get
          :url "https://maps.googleapis.com/maps/api/geocode/json"
          :query-params {:key (:google-maps-api-key config)
                         :address query}})
      :body
      json/decode
      :results
      first
      :geometry
      :location
      (rename-keys {:lat :latitude :lng :longitude})))

(defn shape [query]
  (when-let [result (:geojson (mapquest-osm-search query))]
    (util/simplify-geojson (json/encode result))))
