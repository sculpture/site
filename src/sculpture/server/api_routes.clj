(ns sculpture.server.api-routes
  (:require
    [compojure.core :refer [GET POST PUT DELETE defroutes context]]
    [compojure.handler :refer [api]]
    [compojure.route :as route]
    [environ.core :refer [env]]
    [ring.middleware.format :refer [wrap-restful-format]]
    [ring.middleware.cors :refer [wrap-cors]]
    [ring.middleware.session :refer [wrap-session]]
    [ring.middleware.session.cookie :refer [cookie-store]]
    [ring.util.codec :refer [form-encode]]
    [sculpture.server.db :as db]
    [sculpture.server.oauth :as oauth]
    [sculpture.server.pages.oauth :as pages.oauth]))

(defroutes routes
  (context "/api" _

    (GET "/entities" req
      {:status 200
       :body (db/all)})

    (GET "/session" req
      (if-let [user-id (get-in req [:session :user-id])]
        {:status 200
         :body (db/get-by-id user-id)}
        {:status 401
         :body {:error "You are not logged in"}}))

    (GET "/oauth/:provider/request-token" [provider]
      (case provider
        "google"
        {:status 302
         :body {:ok true}
         :headers {"Location" (str "https://accounts.google.com/o/oauth2/v2/auth?"
                                   (form-encode {:response_type "token"
                                                 :client_id (env :google-client-id)
                                                 :redirect_uri (env :oauth-redirect-uri)
                                                 :scope "email profile"}))}}))
    (GET "/oauth/:provider/post-auth" _
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (pages.oauth/html)})

    (PUT "/oauth/:provider/authenticate" [provider token]
      (if-let [user-info (oauth/get-user-info (keyword provider) token)]
        (do
          (if-let [user (db/select {:type "user"
                                    :email (user-info :email)})]
            {:status 200
             :body user
             :session {:user-id (user :id)}}
            {:status 401
             :body {:error "User has not been approved"}}))
        {:status 401
         :body {:error "User could not be authenticated"}}))

    ; REQUIRE AUTH

    (PUT "/entities" [entity :as req]
      (if-let [user-id (get-in req [:session :user-id])]
        (do
          (db/upsert! entity user-id)
          {:status 200
           :body {:status "OK"}})
        {:status 401
         :body {:error "You must be logged in to perform this action."}}))

    ; TODO insert
    ; TODO delete

    (route/not-found "Page not found")))


(defn cookie-secret []
  (or (env :cookie-secret)
      (throw (Exception. "Must set cookie secret"))))

(defn cookie-secure? []
  (or (env :cookie-secure?)
      false))

(defn cookie-max-age []
  (or (env :cookie-max-age)
      (* 60 60 24 365)))

(def handler
  (-> routes
      wrap-restful-format
      api
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete])
      (wrap-session {:store (cookie-store {:key (cookie-secret)})
                     :cookie-name "sculpture"
                     :cookie-attrs {:secure (cookie-secure?)
                                    :max-age (cookie-max-age)}})))



