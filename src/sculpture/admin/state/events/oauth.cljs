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

(defn message-event-handler [e]
  (let [token (.-data e)]
    (dispatch [:oauth/-remote-auth token])))

(defn attach-message-listener! []
  (js/window.addEventListener "message" message-event-handler))

(reg-event-fx
  :oauth/authenticate
  (fn [_ _]
    (attach-message-listener!)
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
  :oauth/-remote-auth
  (fn [_ [_ token]]
    {:ajax {:method :put
            :uri "/api/oauth/authenticate"
            :params {:token token
                     :provider :google}
            :on-success
            (fn [data]
              (dispatch [:sculpture.user/-handle-user-info data]))}}))


