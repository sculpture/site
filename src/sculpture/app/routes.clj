(ns sculpture.app.routes
  (:require
    [environ.core :refer [env]]
    [compojure.core :refer [GET POST PUT DELETE defroutes context]]
    [compojure.route :refer [resources]]
    [hiccup.core :refer [html]]
    [ring.util.response :refer [resource-response]]))

(defn oauth-html []
  (html
    [:html
     [:body
      [:script
       "var token = window.location.toString().match(/access_token=(.*)&/, '')[1];"
       "window.opener.postMessage(token, window.location);"
       "window.close();"]]]))

(defn index-html []
  (html
    [:html
     [:head
      [:title "Sculpture"]
      [:link {:rel "stylesheet"
              :href "https://unpkg.com/leaflet@1.0.2/dist/leaflet.css"}]
      [:link {:rel "stylesheet"
              :href "https://unpkg.com/leaflet-draw@0.4.9/dist/leaflet.draw-src.css"}]
      [:link {:rel "stylesheet"
              :href "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css"}]]

     [:body
      [:div {:id "app"}]
      [:script {:type "text/javascript"
                :src "https://unpkg.com/stackblur-canvas@1.4.0/dist/stackblur.min.js"}]
      [:script {:type "text/javascript"
                :src "https://unpkg.com/leaflet@1.0.2/dist/leaflet.js"}]
      [:script {:type "text/javascript"
                :src "https://unpkg.com/leaflet-draw@0.4.9/dist/leaflet.draw-src.js"}]
      [:script {:type "text/javascript"
                :src "https://unpkg.com/leaflet-draw-drag@0.4.3/dist/Leaflet.draw.drag.js"}]

      [:script {:type "text/javascript"}
       "window.env = {};"
       (for [k [:mapbox-token :google-client-id :oauth-redirect-uri]]
         (str "window.env['" (name k) "'] = '" (env k) "';"))]

      [:script {:type "text/javascript"
                :src "/js/sculpture.js"}]
      [:script {:type "text/javascript"}
       "sculpture.admin.core.init();"]]]))

(defroutes routes
  (GET "/oauth/" _
    {:status 200
     :body (oauth-html)})

  (GET  "/js/sculpture.js" _
    (if-let [response (resource-response (str "public/js/sculpture.js"))]
      (assoc-in response [:headers "Cache-Control"] "max-age=365000000, immutable")
      {:status 404
       :headers {"Content-Type" "application/javascript"}
       :body (str "alert('JS files are missing. Please compile them with cljsbuild or figwheel.');")}))

  (resources "/")

  (GET "/*" _
    {:status 200
     :body (index-html)}))

(def app
  (-> routes))
