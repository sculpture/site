(ns sculpture.core
  (:require
    [compojure.core :refer [routes]]
    [org.httpkit.server :refer [run-server]]
    [sculpture.api.db :as db]
    [sculpture.api.routes :as api]
    [sculpture.app.routes :as app]))

(def app
  (routes #'api/handler
          #'app/handler))

(defonce server (atom nil))

(defn stop-server!
  []
  (when-let [stop-fn @server]
    (stop-fn :timeout 100)))

(defn start-server!
  [port]
  (db/load!)
  (stop-server!)
  (reset! server (run-server #'app {:port port})))

(defn start! [port]
  (println "starting on port " port)
  (start-server! port))

(defn -main [& args]
  (let [port (Integer/parseInt (first args))]
    (start! port)))
