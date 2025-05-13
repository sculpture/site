(ns sculpture.core
  (:gen-class)
  (:require
    [bloom.omni.core :as omni]
    [sculpture.db.import-export :as db.import-export]
    [sculpture.omni-config :refer [omni-config]]
    [sculpture.server.commands]))

(defn start! []
  (omni/start! omni/system omni-config)
  (db.import-export/import! (db.import-export/all-from-files)))

(defn -main [& _]
  (start!))
