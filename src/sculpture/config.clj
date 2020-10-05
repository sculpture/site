(ns sculpture.config
  (:require
    [bloom.commons.config :as config]))

(def config
  (config/read
   "config.edn"
   [:map
    [:http-port integer?]
    [:environment [:enum :dev :prod]]
    [:db-url [:re #"^postgresql://.*$"]]]))
