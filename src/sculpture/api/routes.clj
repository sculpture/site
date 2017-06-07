(ns sculpture.api.routes
  (:require
    [compojure.core :refer [GET POST PUT DELETE defroutes context]]
    [compojure.handler :refer [api]]
    [ring.middleware.format :refer [wrap-restful-format]]
    [ring.middleware.cors :refer [wrap-cors]]
    [sculpture.api.db :as db]))

(defroutes routes
  (GET "/all" _
    {:status 200
     :body (db/all)}))

(def app
  (-> routes
      wrap-restful-format
      api
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete])))
