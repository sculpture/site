(ns sculpture.admin.state.fx.ajax
  (:require
    [re-frame.core :refer [reg-fx]]
    [ajax.core :as ajax]))

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
