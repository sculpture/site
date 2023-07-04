(ns ^:figwheel-hooks
  sculpture.admin.core
  (:require
    [bloom.commons.pages :as pages]
    [reagent.dom :as rdom]
    [re-frame.core :as re-frame]
    [sculpture.admin.state.api :refer [dispatch-sync!]]
    [sculpture.admin.state.core]
    [sculpture.admin.pages :refer [pages]]
    [sculpture.admin.fields.location]
    [sculpture.admin.fields.geojson]
    [sculpture.admin.fields.enumlookup]
    [sculpture.admin.fields.flexdate]
    [sculpture.admin.fields.radio-related]
    [sculpture.admin.views.app :refer [app-view]]))

(enable-console-print!)

(defn render []
  (rdom/render [app-view] (.. js/document (getElementById "app"))))

(defn ^:export init []
  (dispatch-sync! [:state.core/initialize!])
  (pages/initialize! pages)
  (render))

(defn ^:after-load reload []
  (re-frame/clear-subscription-cache!)
  (render))
