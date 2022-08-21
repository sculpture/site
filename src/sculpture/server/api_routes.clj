(ns sculpture.server.api-routes
  (:require
    [bloom.commons.tada.rpc.server :as tada.rpc]
    [ring.middleware.multipart-params :refer [wrap-multipart-params]]
    [sculpture.config :refer [config]]
    [sculpture.darkroom.core :as darkroom]
    [sculpture.db.core :as db.core]
    [sculpture.db.pg.select :as db.select]
    [sculpture.db.pg.graph :as db.graph]
    [sculpture.db.pg.util :as db.util]
    [sculpture.server.geocode :as geocode]
    [sculpture.server.oauth :as oauth]
    [sculpture.server.pages.oauth :as pages.oauth]))

(defn single-entity-response [entity-type id-or-slug]
  (if-let [entity (db.select/select-entity-with-id-or-slug entity-type id-or-slug)]
    {:status 200
     :body entity}
    {:status 404
     :body {:error "Not Found"}}))

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

    (->> [["category" "categories"]
          ["material"]
          ["artist-tag"]
          ["sculpture-tag"]
          ["region-tag"]
          ["user"]
          ["photo"]
          ["artist"]
          ["region"]]
         (mapcat (fn [[entity-type plural]]
                   (let [plural (or plural (str entity-type "s"))]

                     [[[:get (str "/api/" plural "/")]
                       (fn [_]
                         {:status 200
                          :body (db.select/select-all-with-type entity-type)})]

                      [[:get (str "/api/" entity-type "s/:id-or-slug")]
                       (fn [{{:keys [id-or-slug]} :params}]
                         (single-entity-response entity-type id-or-slug))]]))))

    [
     [[:get "/api/meta"]
      (fn [_]
        {:status 200
         :body (db.select/entity-counts)})]

     [[:get "/api/entities"]
      (fn [_]
        {:status 200
         :body (db.select/select-all)})]

     [[:get "/api/regions/:slug/sculptures"]
      (fn [{{:keys [slug]} :params}]
        {:status 200
         :body (db.select/select-sculptures-for-region slug)})]

     [[:get "/api/materials/:slug/sculptures"]
      (fn [{{:keys [slug]} :params}]
        {:status 200
         :body (db.select/select-sculptures-for-material-slug slug)})]

     [[:get "/api/artists/:slug/sculptures"]
      (fn [{{:keys [slug]} :params}]
        {:status 200
         :body (db.select/select-sculptures-for-artist slug)})]

     [[:get "/api/graph/sculptures"]
      (fn [_]
        {:status 200
         :body (db.graph/select)})]

     [[:get "/api/sculptures/random"]
      (fn [_]
        {:status 302
         :headers {"Location" (str "/api/sculptures/" (db.select/select-random-sculpture-slug))}})]

     [[:get "/api/sculptures/"]
      (fn [{{:keys [decade artist-gender artist-tag sculpture-tag]} :params}]
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
           :body (db.select/select-sculptures-for-artist-gender artist-gender)}))]

     [[:get "/api/sculptures/:slug"]
      (fn [{{:keys [slug]} :params}]
        {:status 200
         :body (db.select/select-sculpture-with-slug slug)})]

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
        (if-let [user-id (get-in request [:session :user-id])]
          {:status 200
           :body (db.select/select-entity-with-id "user" user-id)}
          {:status 401
           :body {:error "You are not logged in"}}))]

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
           :body {:error "User could not be authenticated"}}))]

     ; REQUIRE AUTH

     [[:post "/api/sculptures" ]
      (fn [request]
        (let [{{:keys [id title slug year artist-ids]} :body-params} request]
          (if-let [user-id (request->user-id request)]
            (if (db.core/upsert!
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
            (if (db.core/upsert!
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

     [[:post "/api/photos"]
      (fn [request]
        (let [{{:keys [id file sculpture-id]} :body-params} request]
          (if-let [user-id (request->user-id request)]
            (let [id (java.util.UUID/fromString id)
                  sculpture-id (java.util.UUID/fromString sculpture-id)
                  {:keys [tempfile filename]} file
                  image-data (darkroom/process-image! id tempfile sculpture-id user-id)]
              {:status 200
               :body image-data})
            {:status 401
             :body {:error "You must be logged in to perform this action."}})))
      [wrap-multipart-params]]

     [[:put "/api/entities"]
      (fn [request]
        (let [{{:keys [entity]} :body-params} request]
          (if-let [user-id (request->user-id request)]
            (if (db.core/upsert! entity user-id)
              {:status 200
               :body {:status "OK"}}
              {:status 500
               :body {:error "Error updating or creating entity"}})
            {:status 401
             :body {:error "You must be logged in to perform this action."}})))]

     [[:put "/api/upload"]
      (fn [request]
        (if-let [user-id (request->user-id request)]
          (let [id (java.util.UUID/fromString (get-in request [:params "id"]))
                {:keys [tempfile filename]} (get-in request [:params "file"])
                image-data (darkroom/process-image! id tempfile nil user-id)]
            {:status 200
             :body image-data})
          {:status 401
           :body {:error "You must be logged in to perform this action."}}))
      [wrap-multipart-params]]]))

