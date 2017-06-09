(ns sculpture.admin.state.events.oauth
  (:require
    [re-frame.core :refer [reg-event-fx dispatch]]))

(defn message-event-handler [e]
  (let [token (.-data e)]
    (dispatch [:oauth/-remote-auth token])))

(defn attach-message-listener! []
  (js/window.addEventListener "message" message-event-handler))

(reg-event-fx
  :oauth/authenticate
  (fn [_ _]
    (attach-message-listener!)
    (js/window.open "/api/oauth/google/request-token"
      "Log In to Sculpture"
      "width=500,height=700")
    {}))

(reg-event-fx
  :oauth/-remote-auth
  (fn [_ [_ token]]
    {:ajax {:method :put
            :uri "/api/oauth/google/authenticate"
            :params {:token token}
            :on-success
            (fn [data]
              (dispatch [:sculpture.user/-handle-user-info data]))}}))


