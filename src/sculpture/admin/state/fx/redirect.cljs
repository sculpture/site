(ns sculpture.admin.state.fx.redirect
  (:require
    [sculpture.admin.router :as router]))

(defn redirect-to-fx [path]
  (router/go-to! path))
