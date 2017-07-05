(ns sculpture.server.client-routes
  (:require
    [compojure.core :refer [GET POST PUT DELETE defroutes context]]
    [compojure.route :refer [resources]]
    [ring.util.response :refer [resource-response]]
    [sculpture.server.pages.index :as pages.index]))

(defroutes routes

  (GET  "/js/sculpture.js" _
    (if-let [response (resource-response "public/js/sculpture.js")]
      (assoc-in response [:headers "Cache-Control"] "max-age=365000000, immutable")
      {:status 404
       :headers {"Content-Type" "application/javascript; charset=utf-8"}
       :body (str "alert('JS files are missing. Please compile them with cljsbuild or figwheel.');")}))

  (resources "/")

  (GET "/*" _
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (pages.index/html)}))

(def handler
  (-> routes))
