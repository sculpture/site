(ns sculpture.admin.router
 (:require
    [secretary.core :as secretary]
    [accountant.core :as accountant]))

(defn go-to! [path]
  (accountant/navigate! path))

(defn init-router! []
  (accountant/configure-navigation! {:nav-handler secretary/dispatch!
                                     :path-exists? secretary/locate-route})
  (accountant/dispatch-current!))
