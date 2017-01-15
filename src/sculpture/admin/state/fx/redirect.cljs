(ns sculpture.admin.state.fx.redirect
  (:require
    [re-frame.core :refer [reg-fx]]
    [sculpture.admin.router :as router]))

(reg-fx
  :redirect-to
  (fn [path]
    (router/go-to! path)))
