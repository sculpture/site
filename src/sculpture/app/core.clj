(ns sculpture.app.core
  (:require
    [org.httpkit.server :refer [run-server]]
    [sculpture.app.routes :refer [app]]))

(defonce server (atom nil))

(defn stop-server!
  []
  (when-let [stop-fn @server]
    (stop-fn :timeout 100)))

(defn start-server!
  [port]
  (stop-server!)
  (reset! server (run-server #'app {:port port})))



