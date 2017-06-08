(ns sculpture.admin.state.events.oauth
  (:require
    [clojure.string :as string]
    [re-frame.core :refer [reg-event-fx dispatch]]
    [ajax.core :as ajax]
    [sculpture.admin.env :refer [env]])
  (:import
    [goog.Uri]))

(def uri "https://accounts.google.com/o/oauth2/v2/auth")

(defn client-id []
  (env :google-client-id))

(defn redirect-uri []
  (env :oauth-redirect-uri))

(defn ->query-string [m]
  (.toString (.createFromMap goog.Uri.QueryData (clj->js m))))

(reg-event-fx
  :init-oauth
  (fn [_ _]
    (js/window.addEventListener
      "message"
      (fn [e]
        (let [token (.-data e)]
          (dispatch [:handle-oauth-token token]))))
    {}))

(reg-event-fx
  :request-oauth-token
  (fn [_ _]
    (js/window.open
      (str uri "?"
           (->query-string {:response_type "token"
                            :client_id (client-id)
                            :redirect_uri (redirect-uri)
                            :scope "email profile"}))
      "Log In with Google"
      "width=500,height=700")
    {}))

(reg-event-fx
  :handle-oauth-token
  (fn [{db :db} [_ token]]
    {:db (assoc-in db [:user :token] token)
     :dispatch [:validate-oauth-token]}))

(reg-event-fx
  :validate-oauth-token
  (fn [{db :db} _]
    (ajax/ajax-request
      {:uri "https://www.googleapis.com/oauth2/v3/tokeninfo"
       :method :get
       :params {:access_token (get-in db [:user :token])}
       :response-format (ajax/json-response-format {:keywords? true})
       :handler (fn [[ok response]]
                  (if ok
                    (if (= (response :aud) (client-id))
                      (dispatch [:request-oauth-user-info])
                      (println "TOKEN INVALID"))
                    (println "TOKEN INVALID")))})
    {}))

(reg-event-fx
  :request-oauth-user-info
  (fn [{db :db} _]
    (ajax/ajax-request
      {:uri "https://www.googleapis.com/oauth2/v1/userinfo?alt=json"
       :method :get
       :params {:access_token (get-in db [:user :token])}
       :response-format (ajax/json-response-format {:keywords? true})
       :handler (fn [[ok response]]
                  (if ok
                    (dispatch [:handle-oauth-user-info response])
                    (println "ERROR")))})
    {}))

(reg-event-fx
  :handle-oauth-user-info
  (fn [{db :db} [_ {:keys [name picture email]}]]
    {:db (-> db
             (assoc-in [:user :name] name)
             (assoc-in [:user :avatar] picture)
             (assoc-in [:user :email] email))}))
