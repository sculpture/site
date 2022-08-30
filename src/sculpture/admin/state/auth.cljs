(ns sculpture.admin.state.auth
  (:require
    [re-frame.core :refer [dispatch reg-sub]]
    [sculpture.admin.state.util :refer [reg-event-fx]]))

(reg-event-fx
  :state.auth/check-auth!
  (fn [_ _]
    {:ajax {:method :get
            :uri "/api/session"
            :on-success
            (fn [data]
              (when data
                (dispatch [::store-user-info! data])))}}))

(reg-event-fx
  ::store-user-info!
  (fn [{db :db} [_ user]]
    {:db (assoc db :db/user user)}))

(defn message-event-handler [e]
  (let [token (.-data e)]
    (dispatch [::remote-oauth! token])))

(defn attach-message-listener! []
  (js/window.addEventListener "message" message-event-handler))

(reg-event-fx
  :state.auth/start-oauth!
  (fn [_ _]
    (attach-message-listener!)
    (js/window.open "/api/oauth/google/request-token"
      "Log In to Sculpture"
      "width=500,height=700")
    {}))

(reg-event-fx
  ::remote-oauth!
  (fn [_ [_ token]]
    {:ajax {:method :put
            :uri "/api/oauth/google/authenticate"
            :params {:token token}
            :on-success
            (fn [data]
              (dispatch [::store-user-info! data]))}}))

(reg-event-fx
  :state.auth/log-out!
  (fn [{db :db} _]
    {:ajax {:method :delete
            :uri "/api/session"}
     :db (assoc db :db/user nil)
     :dispatch [:state.core/set-main-page! nil]}))

;; SUBS

(reg-sub
  :state.auth/user
  (fn [db _]
    (db :db/user)))
