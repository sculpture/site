(ns sculpture.admin.core
  (:require
    [reagent.core :as r]
    [re-frame.core :refer [dispatch-sync]]
    [sculpture.specs.core]
    [sculpture.admin.fx]
    [sculpture.admin.events]
    [sculpture.admin.subs]
    [sculpture.admin.routes :refer [init-router!]]
    [sculpture.admin.views.app :refer [app-view]]))

(enable-console-print!)

(defn render []
  (r/render-component [app-view] (.. js/document (getElementById "app"))))

(defn init []
  (dispatch-sync [:init])
  (init-router!)
  (render))

(defn reload []
  (render))
