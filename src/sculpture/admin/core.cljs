(ns sculpture.admin.core
  (:require
    [reagent.core :as r]
    [sculpture.admin.state.core :refer [dispatch-sync!]]
    [sculpture.admin.state.events]
    [sculpture.admin.state.subs]
    [sculpture.specs.core]
    [sculpture.admin.routes]
    [sculpture.admin.router :refer [init-router!]]
    [sculpture.admin.views.app :refer [app-view]]))

(enable-console-print!)

(defn render []
  (r/render-component [app-view] (.. js/document (getElementById "app"))))

(defn ^:export init []
  (dispatch-sync! [:init])
  (init-router!)
  (render))

(defn reload []
  (render))
