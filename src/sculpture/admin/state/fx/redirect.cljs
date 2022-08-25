(ns sculpture.admin.state.fx.redirect
  (:require
    [bloom.commons.pages :as pages]))

(defn redirect-to-fx [path]
  (pages/navigate-to! path))
