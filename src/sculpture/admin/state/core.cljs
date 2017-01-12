(ns sculpture.admin.state.core
  (:require
    [re-frame.core :as reframe]
    [sculpture.admin.state.fx.dispatch-debounce]
    [sculpture.admin.state.fx.ajax]
    [sculpture.admin.state.events]
    [sculpture.admin.state.subs]))

(def dispatch! reframe/dispatch)

(def dispatch-sync! reframe/dispatch-sync)

(def subscribe reframe/subscribe)
