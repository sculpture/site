(ns sculpture.server.api-routes
  (:require
    [compojure.core :refer [GET POST PUT DELETE defroutes context]]
    [compojure.handler :refer [api]]
    [compojure.route :as route]
    [environ.core :refer [env]]
    [ring.middleware.multipart-params :refer [wrap-multipart-params]]
    [ring.middleware.format :refer [wrap-restful-format]]
    [ring.middleware.cors :refer [wrap-cors]]
    [ring.middleware.session :refer [wrap-session]]
    [ring.middleware.session.cookie :refer [cookie-store]]
    [sculpture.darkroom.core :as darkroom]
    [sculpture.db.core :as db.core]
    [sculpture.db.pg.select :as db.select]
    [sculpture.server.oauth :as oauth]
    [sculpture.server.pages.oauth :as pages.oauth]))

(defroutes routes
  (context "/api" _

    (GET "/entities" req
      {:status 200
       :body (db.select/select-all)})

    (GET "/materials/" _
      {:status 200
       :body (db.select/select-all-with-type "material")})

    (GET "/artist-tags/" _
      {:status 200
       :body (db.select/select-all-with-type "artist-tag")})

    (GET "/sculpture-tags/" _
      {:status 200
       :body (db.select/select-all-with-type "sculpture-tag")})

    (GET "/region-tags/" _
      {:status 200
       :body (db.select/select-all-with-type "region-tags")})

    (GET "/users/" _
      {:status 200
       :body (db.select/select-all-with-type "user")})

    (GET "/photos/" _
      {:status 200
       :body (db.select/select-all-with-type "photo")})

    (GET "/artists/" _
      {:status 200
       :body (db.select/select-all-with-type "artist")})

    (GET "/artists/:slug" [slug]
      {:status 200
       :body (db.select/select-artist-with-slug slug)})

    (GET "/artists/:slug/sculptures" [slug]
      {:status 200
       :body (db.select/select-sculptures-for-artist slug)})

    (GET "/sculptures/random" []
      {:status 302
       :headers {"Location" (str "./" (db.select/select-random-sculpture-slug))}})

    (GET "/sculptures/" [decade artist-gender artist-tag sculpture-tag]
      (cond
        decade
        {:status 200
         :body (db.select/select-sculptures-for-decade (Integer. decade))}

        artist-tag
        {:status 200
         :body (db.select/select-sculptures-for-artist-tag-slug artist-tag)}

        sculpture-tag
        {:status 200
         :body (db.select/select-sculptures-for-sculpture-tag-slug sculpture-tag)}

        artist-gender
        {:status 200
         :body (db.select/select-sculptures-for-artist-gender artist-gender)}))

    (GET "/sculptures/:slug" [slug]
      {:status 200
       :body (db.select/select-sculpture-with-slug slug)})

    (GET "/regions/" _
      {:status 200
       :body (db.select/select-regions)})

    (GET "/regions/:slug/sculptures" [slug]
      {:status 200
       :body (db.select/select-sculptures-for-region slug)})

    ; SESSION

    (GET "/session" req
      (if-let [user-id (get-in req [:session :user-id])]
        {:status 200
         :body (db.select/select-entity-with-id "user" user-id)}
        {:status 401
         :body {:error "You are not logged in"}}))

    ; OAUTH

    (GET "/oauth/:provider/request-token" [provider]
      (if-let [request-token-url (oauth/request-token-url (keyword provider))]
        {:status 302
         :body {:ok true}
         :headers {"Location" request-token-url}}
        {:status 400
         :body {:error "Unsupported oauth provider"}}))

    (GET "/oauth/:provider/post-auth" _
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (pages.oauth/html)})

    (PUT "/oauth/:provider/authenticate" [provider token]
      (if-let [oauth-user-info (oauth/get-user-info (keyword provider) token)]
        (if-let [user (db.select/select-user-with-email (oauth-user-info :email))]
          (do
            (when (or (not= (:name oauth-user-info) (:name user))
                      (not= (:avatar oauth-user-info) (:avatar user)))
              (db.core/upsert! (merge user oauth-user-info) (:id user)))
            {:status 200
             :body (merge user oauth-user-info)
             :session {:user-id (user :id)}})
          {:status 401
           :body {:error "User has not been approved"}})
        {:status 401
         :body {:error "User could not be authenticated"}}))

    ; REQUIRE AUTH

    (PUT "/entities" [entity :as req]
      (if-let [user-id (get-in req [:session :user-id])]
        (do
          (db.core/upsert! entity user-id)
          {:status 200
           :body {:status "OK"}})
        {:status 401
         :body {:error "You must be logged in to perform this action."}}))

    (PUT "/upload" req
      (if-let [user-id (get-in req [:session :user-id])]
        (let [id (java.util.UUID/fromString (get-in req [:params "id"]))
              {:keys [tempfile filename]} (get-in req [:params "file"])
              image-data (darkroom/process-image! id tempfile user-id)]
          {:status 200
           :body image-data})
        {:status 401
         :body {:error "You must be logged in to perform this action."}}))

    (route/not-found "Page not found")))


(def cookie-secret (or (env :cookie-secret)
                       (println "WARNING: COOKIE SECRET NOT SET")))
(def cookie-secure? (or (env :cookie-secure?) false))
(def cookie-max-age (or (env :cookie-max-age) (* 60 60 24 365)))

(def handler
  (-> routes
      wrap-restful-format
      wrap-multipart-params
      api
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete])
      (wrap-session {:store (cookie-store {:key cookie-secret})
                     :cookie-name "sculpture"
                     :cookie-attrs {:secure cookie-secure?
                                    :max-age cookie-max-age}})))



