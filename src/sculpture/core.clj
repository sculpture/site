(ns sculpture.core
  (:gen-class)
  (:require
    [bloom.omni.core :as omni]
    [sculpture.omni-config :refer [omni-config]]
    [sculpture.server.commands]))

(defn start! []
  (omni/start! omni/system omni-config))

(defn -main [& _]
  (start!))
