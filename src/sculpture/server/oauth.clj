(ns sculpture.server.oauth
  (:require
    [clojure.data.json :as json]
    [environ.core :refer [env]]
    [org.httpkit.client :as http]
    [ring.util.codec :refer [form-encode]]))

(defn request-token-url [provider]
  (case provider
    :google
    (str "https://accounts.google.com/o/oauth2/v2/auth?"
         (form-encode {:response_type "token"
                       :client_id (env :google-client-id)
                       :redirect_uri (env :oauth-redirect-uri)
                       :scope "email profile"}))
    nil))

(defn valid-token? [provider token]
  (case provider
    :google
    (let [resp (-> @(http/request
                      {:method :get
                       :url (str "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=" token)})
                   :body
                   (json/read-str :key-fn keyword))]
      (= (resp :aud) (env :google-client-id)))))

(defn get-user-info [provider token]
  (when (valid-token? provider token)
    (case provider
      :google
      (let [resp (-> @(http/request
                        {:method :get
                         :url (str "https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=" token)})
                     :body
                     (json/read-str :key-fn keyword))]
        {:name (resp :name)
         :email (resp :email)
         :avatar (resp :picture)}))))
