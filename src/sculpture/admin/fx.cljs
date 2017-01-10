(ns sculpture.admin.fx
  (:require
    [ajax.core :as ajax]
    [re-frame.router :as router]
    [re-frame.core :refer [reg-fx]]
    [re-frame.loggers :refer [console]]))

(reg-fx
  :ajax
  (fn [{:keys [uri method params body on-success on-error headers]
        :or {on-success identity
             on-error (fn [r]
                        (.error js/console "Ajax request error" (pr-str r)))}}]
    (ajax/ajax-request
      {:uri uri
       :method method
       :body body
       :params params
       :headers headers
       :format (ajax/transit-request-format)
       :response-format (ajax/transit-response-format)
       :handler
       (fn [[ok response]]
         (if ok
           (on-success response)
           (on-error response)))})))

(def debounced-events (atom {}))

(defn cancel-timeout [id]
  (js/clearTimeout (:timeout (@debounced-events id)))
  (swap! debounced-events dissoc id))

(reg-fx
  :dispatch-debounce
  (fn [dispatches]
    (let [dispatches (if (sequential? dispatches) dispatches [dispatches])]
      (doseq [{:keys [id action dispatch timeout]
               :or   {action :dispatch}}
              dispatches]
        (case action
          :dispatch (do
                      (cancel-timeout id)
                      (swap! debounced-events assoc id
                             {:timeout  (js/setTimeout (fn []
                                                          (swap! debounced-events dissoc id)
                                                          (router/dispatch dispatch))
                                                        timeout)
                              :dispatch dispatch}))
          :cancel (cancel-timeout id)
          :flush (let [ev (get-in @debounced-events [id :dispatch])]
                   (cancel-timeout id)
                   (router/dispatch ev))
          (console :warn "re-frame: ignoring bad :dispatch-debounce action:" action "id:" id))))))
