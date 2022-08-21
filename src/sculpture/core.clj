(ns sculpture.core
  (:gen-class)
  (:require
    [bloom.omni.core :as omni]
    [sculpture.server.api-routes :as api]
    [sculpture.config :refer [config]]
    [sculpture.server.commands]))

(def omni-config
  {:omni/http-port (:http-port config)
   :omni/environment (:environment config)
   :omni/title "Sculpture"
   :omni/cljs {:main "sculpture.admin.core"}
   #_#_:omni/css {:tailwind? true
                  :tailwind-opts {:base-css-rules '[girouette.tw.preflight/preflight-v2_0_3]}}
   :omni/auth {:cookie {:name "sculpture"
                        :secret (:cookie-secret config)
                        :same-site :strict}}
   :omni/api-routes #'api/routes
   :omni/html-head-includes [[:link {:rel "stylesheet"
                                     :href "https://unpkg.com/leaflet@1.0.3/dist/leaflet.css"}]
                             [:link {:rel "stylesheet"
                                     :href "https://unpkg.com/leaflet-draw@0.4.10/dist/leaflet.draw-src.css"}]
                             [:link {:rel "stylesheet"
                                     :href "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css"}]
                             [:script {:type "text/javascript"}
                              "window.env = {};"
                              (for [k [:mapbox-token]]
                                (str "window.env['" (name k) "'] = '" (config k) "';"))]]
   :omni/js-scripts [{:src "https://unpkg.com/stackblur-canvas@1.4.0/dist/stackblur.min.js"}
                     {:src "https://unpkg.com/leaflet@1.0.3/dist/leaflet.js"}
                     {:src "https://unpkg.com/leaflet-draw@0.4.10/dist/leaflet.draw-src.js"}
                     {:src "https://unpkg.com/leaflet-draw-drag@0.4.4/dist/Leaflet.draw.drag.js"}]})

(defn start! []
  (omni/start! omni/system omni-config))

(defn -main [& _]
  (start!))
