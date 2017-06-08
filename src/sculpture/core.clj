(ns sculpture.core
  (:require
    [sculpture.api.core :as api]
    [sculpture.app.core :as app]))

(defn start! [api-port app-port]
  (println "starting API on port " api-port)
  (api/start-server! api-port)
  (println "starting APP on port " app-port)
  (app/start-server! app-port))

(defn -main  [& args]
  (let [api-port (Integer/parseInt (first args))
        app-port (Integer/parseInt (second args))]
    (start! api-port app-port)))
