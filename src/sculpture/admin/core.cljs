(ns sculpture.admin.core
  (:require
    [reagent.dom :as rdom]
    [re-frame.core :as re-frame]
    [sculpture.admin.state.core :refer [dispatch-sync!]]
    [sculpture.admin.state.events]
    [sculpture.admin.state.subs]
    [sculpture.admin.routes]
    [sculpture.admin.fields.location]
    [sculpture.admin.fields.geojson]
    [sculpture.admin.fields.flexdate]
    [sculpture.admin.router :refer [init-router!]]
    [sculpture.admin.views.app :refer [app-view]]))

(enable-console-print!)

(defn render []
  (rdom/render [app-view] (.. js/document (getElementById "app"))))

(defn ^:export init []
  (dispatch-sync! [:init])
  (init-router!)
  (render))

(defn reload []
  (re-frame/clear-subscription-cache!)
  (render))
