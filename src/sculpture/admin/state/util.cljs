(ns sculpture.admin.state.util
  (:require
    [bloom.commons.pages :as pages]
    [bloom.commons.ajax :as ajax]
    [bloom.commons.tada.rpc.client :as tada.rpc]
    [bloom.omni.fx.dispatch-debounce :as dispatch-debounce]
    [malli.core :as m]
    [malli.error :as me]
[re-frame.core :as re-frame]
    [sculpture.admin.state.spec :as spec]
    [sculpture.admin.state.fx.upload :refer [upload-fx]]))

(re-frame/reg-fx :tada (tada.rpc/make-dispatch {:base-path "/api/tada"}))
(re-frame/reg-fx :ajax ajax/request)
(re-frame/reg-fx :upload upload-fx)
(re-frame/reg-fx :redirect-to pages/navigate-to!)
(re-frame/reg-fx :dispatch-debounce dispatch-debounce/fx)

(if ^boolean goog.DEBUG
  (do
    (def validate-schema-interceptor
      (re-frame/after
        (fn [db [event-id]]
          (when-let [errors (m/explain spec/AppState db)]
            (js/console.error
              (str
                "Event " event-id
                " caused the state to be invalid:\n")
              (str (me/humanize errors)))))))
    (defn reg-event-fx
      ([id handler-fn]
       (reg-event-fx id nil handler-fn))
      ([id interceptors handler-fn]
       (re-frame/reg-event-fx
         id
         [validate-schema-interceptor
          interceptors]
         handler-fn))))
  (def reg-event-fx re-frame/reg-event-fx))
