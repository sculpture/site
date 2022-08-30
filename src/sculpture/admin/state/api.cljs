(ns sculpture.admin.state.api
  (:require
    [re-frame.core :as reframe]))

(def dispatch! reframe/dispatch)

(def dispatch-sync! reframe/dispatch-sync)

(def subscribe reframe/subscribe)
