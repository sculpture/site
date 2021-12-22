(ns sculpture.server.core
  (:gen-class)
  (:require
    [compojure.core :refer [routes]]
    [org.httpkit.server :refer [run-server]]
    [sculpture.config :refer [config]]
    [sculpture.server.api-routes :as api]
    [sculpture.server.client-routes :as client]))

(def app
  (routes #'api/handler
          #'client/handler))

(defonce server (atom nil))

(defn stop-server!
  []
  (when-let [stop-fn @server]
    (stop-fn :timeout 100)))

(defn start-server!
  []
  (let [port (:http-port config)]
    (stop-server!)
    (println "starting on port" port)
    (reset! server (run-server #'app {:port port}))))

(defn -main [& _]
  (start-server!))
