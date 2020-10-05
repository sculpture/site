(ns sculpture.server.geocode
  (:require
    [clojure.set :refer [rename-keys]]
    [environ.core :refer [env]]
    [org.httpkit.client :as http]
    [sculpture.json :as json]
    [sculpture.db.pg.util :as util]))

(defn mapquest-osm-search [query]
  (-> @(http/request
         {:method :get
          :url "http://open.mapquestapi.com/nominatim/v1/search.php"
          :query-params {:key (env :mapquest-api-key)
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
          :query-params {:key (env :location-iq-api-key)
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
          :query-params {:key (env :mapquest-api-key)
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
          :query-params {:key (env :google-maps-api-key)
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
