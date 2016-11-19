(ns sculpture.core
  (:require
    [reagent.core :as r]))

(enable-console-print!)

(defn appview []
  [:input {:type "file" :multiple true}])

(defn render []
  (r/render-component [appview] (.. js/document (getElementById "app"))))

(defn init []
  (render))

(defn reload []
  (render))
