(ns sculpture.admin.routes
  (:require
    [secretary.core :as secretary :refer-macros [defroute]]
    [accountant.core :as accountant]
    [sculpture.admin.state.core :refer [dispatch!]]))

(defroute root-path "/" []
  (dispatch! [:set-page {:type :root}]))

(defroute entity-path "/entity/:id" [id]
  (dispatch! [:set-page {:type :entity
                         :id id}]))

(defroute entity-edit-path "/entity/:id/edit" [id]
  (dispatch! [:set-page {:type :entity
                         :edit? true
                         :id id}]))

(defn go-to [path]
  (accountant/navigate! path))

(defn init-router! []
  (accountant/configure-navigation! {:nav-handler secretary/dispatch!
                                     :path-exists? secretary/locate-route})
  (accountant/dispatch-current!))
