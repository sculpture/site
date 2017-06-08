(ns sculpture.api.core
  (:require
    [org.httpkit.server :refer [run-server]]
    [sculpture.api.routes :refer [app]]
    [sculpture.api.db :as db]))

(defonce server (atom nil))

(defn stop-server!
  []
  (when-let [stop-fn @server]
    (stop-fn :timeout 100)))

(defn start-server!
  [port]
  (stop-server!)
  (db/load!)
  (reset! server (run-server #'app {:port port})))



