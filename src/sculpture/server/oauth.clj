(ns sculpture.server.oauth
  (:require
    [org.httpkit.client :as http]
    [ring.util.codec :refer [form-encode]]
    [sculpture.config :refer [config]]
    [sculpture.json :as json]))

(defn request-token-url [provider]
  (case provider
    :google
    (str "https://accounts.google.com/o/oauth2/v2/auth?"
         (form-encode {:response_type "token"
                       :client_id (:google-client-id config)
                       :redirect_uri (:oauth-redirect-uri config)
                       :scope "email profile"}))
    nil))

(defn valid-token? [provider token]
  (case provider
    :google
    (let [resp (-> @(http/request
                      {:method :get
                       :url (str "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=" token)})
                   :body
                   json/decode)]
      (= (resp :aud) (:google-client-id config)))))

(defn get-user-info [provider token]
  (when (valid-token? provider token)
    (case provider
      :google
      (let [resp (-> @(http/request
                        {:method :get
                         :url (str "https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=" token)})
                     :body
                     json/decode)]
        {:name (resp :name)
         :email (resp :email)
         :avatar (resp :picture)}))))
