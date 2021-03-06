(ns sculpture.server.pages.index
  (:require
    [environ.core :refer [env]]
    [hiccup.core :as hiccup]
    [sculpture.server.digest :as digest]))

(defn html []
  (hiccup/html
    [:html
     [:head
      [:title "Sculpture"]
      [:link {:rel "stylesheet"
              :href "https://unpkg.com/leaflet@1.0.3/dist/leaflet.css"}]
      [:link {:rel "stylesheet"
              :href "https://unpkg.com/leaflet-draw@0.4.10/dist/leaflet.draw-src.css"}]
      [:link {:rel "stylesheet"
              :href "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css"}]]

     [:body
      [:div {:id "app"}]
      [:script {:type "text/javascript"
                :src "https://unpkg.com/stackblur-canvas@1.4.0/dist/stackblur.min.js"}]
      [:script {:type "text/javascript"
                :src "https://unpkg.com/leaflet@1.0.3/dist/leaflet.js"}]
      [:script {:type "text/javascript"
                :src "https://unpkg.com/leaflet-draw@0.4.10/dist/leaflet.draw-src.js"}]
      [:script {:type "text/javascript"
                :src "https://unpkg.com/leaflet-draw-drag@0.4.4/dist/Leaflet.draw.drag.js"}]

      [:script {:type "text/javascript"}
       "window.env = {};"
       (for [k [:mapbox-token]]
         (str "window.env['" (name k) "'] = '" (env k) "';"))]

      (let [digest (digest/from-file "public/js/sculpture.js")]
        [:script {:type "text/javascript"
                  :src (str "/js/sculpture.js?v=" digest)
                  :crossorigin "anonymous"
                  :integrity (str "sha256" "-" digest)}])
      [:script {:type "text/javascript"}
       "sculpture.admin.core.init();"]]]))
