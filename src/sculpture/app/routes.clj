(ns sculpture.app.routes
  (:require
    [compojure.core :refer [GET POST PUT DELETE defroutes context]]
    [compojure.route :refer [resources]]
    [ring.util.response :refer [resource-response]]
    [sculpture.app.pages.index :as pages.index]
    [sculpture.app.pages.oauth :as pages.oauth]))

(defroutes routes
  (GET "/oauth/" _
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (pages.oauth/html)})

  (GET  "/js/sculpture.js" _
    (if-let [response (resource-response (str "public/js/sculpture.js"))]
      (assoc-in response [:headers "Cache-Control"] "max-age=365000000, immutable")
      {:status 404
       :headers {"Content-Type" "application/javascript"}
       :body (str "alert('JS files are missing. Please compile them with cljsbuild or figwheel.');")}))

  (resources "/")

  (GET "/*" _
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (pages.index/html)}))

(def handler
  (-> routes))
