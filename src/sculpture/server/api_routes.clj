(ns sculpture.server.api-routes
  (:require
    [clojure.edn :as edn]
    [bloom.commons.tada.rpc.server :as tada.rpc]
    [ring.middleware.multipart-params :refer [wrap-multipart-params]]
    [ring.util.codec :refer [url-decode]]
    [sculpture.config :refer [config]]
    [sculpture.darkroom.core :as darkroom]
    [sculpture.db.api :as db]
    [sculpture.db.pg.select :as db.select]
    [sculpture.db.pg.util :as db.util]
    [sculpture.server.geocode :as geocode]
    [sculpture.server.oauth :as oauth]
    [sculpture.server.pages.oauth :as pages.oauth]))

(defn request->user-id [request]
  (or (get-in request [:session :user-id])
      ; TODO this header should be encrypted or signed
      (some-> (get-in request [:headers "user-id"])
              (java.util.UUID/fromString))))

(def routes
  (concat
    [[[:post "/api/tada/*"]
      (tada.rpc/make-handler
        {:extra-params (fn [request]
                         {:user-id (get-in request [:session :user-id])})})]]


    [
     [[:get "/api/meta"]
      (fn [_]
        {:status 200
         :body (db.select/entity-counts)})]

     [[:post "/api/eql"]
      (fn [{:keys [body-params form-params]} ]
        (let [params (or body-params
                         form-params)
              {:keys [identifier pattern]} params]
          {:status 200
           :body (db/query identifier pattern)}))]

     [[:get "/api/eql"]
      (fn [{{:keys [identifier pattern]} :params}]
        (let [identifier (edn/read-string (url-decode identifier))
              pattern (edn/read-string (url-decode pattern))]
          {:status 200
           :body (db/query identifier pattern)}))]




     [[:get "/api/sculptures/random"]
      (fn [_]
        {:status 302
         :headers {"Location" (str "/api/sculptures/" (db.select/select-random-sculpture-slug))}})]


     ; UTIL

     [[:get "/api/util/geocode"]
      (fn [{{:keys [query]} :params}]
        (if-let [result (geocode/google-geocode query)]
          {:status 200
           :body result}
          {:status 404
           :body {:error "Not Found"}}))]

     [[:get "/api/util/shape"]
      (fn [{{:keys [query]} :params}]
        (if-let [result (geocode/shape query)]
          {:status 200
           :body {:geojson result}}
          {:status 404
           :body {:error "Not Found"}}))]

     [[:put "/api/util/simplify"]
      (fn [{{:keys [geojson]} :body-params}]
        {:status 200
         :body {:geojson (db.util/simplify-geojson geojson)}})]

     ; SESSION
     [[:get "/api/session"]
      (fn [request]
        (case (:environment config)
          :dev
          (let [user (db/query
                      {:user/id #uuid "013ec717-531b-4b30-bacf-8a07f33b0d43"}
                      [:user/id
                       :user/email
                       :user/name
                       :user/avatar])]
            {:status 200
             :session {:user-id (:user/id user)}
             :body user})

          :prod
          (if-let [user-id (get-in request [:session :user-id])]
            {:status 200
             :body (db/query
                      {:user/id user-id}
                      [:user/id
                       :user/email
                       :user/name
                       :user/avatar])}
            {:status 200
             :body nil})))]

     [[:delete "/api/session"]
      (fn [_]
        {:status 200
         :session nil
         :body {:ok true}})]

     ; OAUTH

     [[:get "/api/oauth/:provider/request-token"]
      (fn [{{:keys [provider]} :params}]
        (if-let [request-token-url (oauth/request-token-url (keyword provider))]
          {:status 302
           :body {:ok true}
           :headers {"Location" request-token-url}}
          {:status 400
           :body {:error "Unsupported oauth provider"}}))]

     [[:get "/api/oauth/:provider/post-auth"]
      (fn [_]
        {:status 200
         :headers {"Content-Type" "text/html"}
         :body (pages.oauth/html)})]

     [[:put "/api/oauth/:provider/authenticate"]
      (fn [{{:keys [provider]} :params
            {:keys [token]} :body-params}]
        (if-let [oauth-user-info (oauth/get-user-info (keyword provider) token)]
          (if-let [user (db/query
                         {:user/email (:email oauth-user-info)}
                         [:user/id :user/name :user/email :user/avatar])]
            (let [updated-user (merge user
                                      {:user/name (:name oauth-user-info)
                                       :user/avatar (:avatar oauth-user-info)})]
              (when (or (not= (:name oauth-user-info) (:user/name user))
                        (not= (:avatar oauth-user-info) (:user/avatar user)))
                (db/upsert! updated-user (:user/id user)))
              {:status 200
               :body updated-user
               :session {:user-id (:user/id user)}})
            {:status 401
             :body {:error "User has not been approved"}})
          {:status 401
           :body {:error "User could not be authenticated"}}))]

     ; REQUIRE AUTH

     [[:post "/api/sculptures" ]
      (fn [request]
        (let [{{:keys [id title slug year artist-ids]} :body-params} request]
          (if-let [user-id (request->user-id request)]
            (if (db/upsert!
                  {:id id
                   :type "sculpture"
                   :slug slug
                   :title title
                   :year year
                   :artist-ids artist-ids}
                  user-id)
              {:status 200
               :body {:status "OK"}}
              {:status 500
               :body {:error "Error creating sculpture"}})
            {:status 401
             :body {:error "You must be logged in to perform this action."}})))]

     [[:post "/api/artists"]
      (fn [request]
        (let [{{:keys [id name slug :as req]} :body-params} request]
          (if-let [user-id (request->user-id req)]
            (if (db/upsert!
                  {:id id
                   :name name
                   :type "artist"
                   :slug slug}
                  user-id)
              {:status 200
               :body {:status "OK"}}
              {:status 500
               :body {:error "Error creating artist"}})
            {:status 401
             :body {:error "You must be logged in to perform this action."}})))]

     [[:put "/api/entities"]
      (fn [request]
        (let [{{:keys [entity]} :body-params} request]
          (if-let [user-id (request->user-id request)]
            (if (db/upsert! entity user-id)
              {:status 200
               :body {:status "OK"}}
              {:status 500
               :body {:error "Error updating or creating entity"}})
            {:status 401
             :body {:error "You must be logged in to perform this action."}})))]

     [[:post "/api/photos"]
      (fn [request]
        (let [{{:keys [id file sculpture-id]} :body-params} request]
          (if-let [user-id (request->user-id request)]
            (let [id (java.util.UUID/fromString id)
                  sculpture-id (java.util.UUID/fromString sculpture-id)
                  {:keys [tempfile filename]} file
                  image-data (darkroom/process-image! id tempfile)]
              (db/upsert! {:photo/id id
                           :photo/type "photo"
                           :photo/user-id user-id
                           :photo/colors (vec (image-data :colors))
                           :photo/captured-at (image-data :created-at)
                           :photo/width (get-in image-data [:dimensions :width])
                           :photo/height (get-in image-data [:dimensions :height])
                           :photo/location (image-data :location)
                           :photo/sculpture-id sculpture-id}
                          user-id)
              {:status 200
               :body {:photo-id id}})
            {:status 401
             :body {:error "You must be logged in to perform this action."}})))
      [wrap-multipart-params]]

     [[:put "/api/upload"]
      (fn [request]
        (if-let [user-id (request->user-id request)]
          (let [id (java.util.UUID/fromString (get-in request [:params "id"]))
                {:keys [tempfile filename]} (get-in request [:params "file"])
                image-data (darkroom/process-image! id tempfile)]
            (db/upsert!
              {:photo/id id
               :photo/type "photo"
               :photo/user-id user-id
               :photo/colors (vec (image-data :colors))
               :photo/captured-at (image-data :created-at)
               :photo/width (get-in image-data [:dimensions :width])
               :photo/height (get-in image-data [:dimensions :height])
               :photo/location (image-data :location)
               :photo/sculpture-id nil}
              user-id)
            {:status 200
             :body {:photo-id id}})
          {:status 401
           :body {:error "You must be logged in to perform this action."}}))
      [wrap-multipart-params]]

     [[:any "/api/*"]
      (fn [_]
        {:status 400})]]))
