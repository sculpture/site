(ns sculpture.admin.routes
  (:require
    [secretary.core :refer [defroute]]
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
