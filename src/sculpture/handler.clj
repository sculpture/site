(ns sculpture.handler)

(defn force-index [request]
  (case (request :uri)
    "oauth/"
    {:status 200
     :body (slurp "./resources/public/index.html")}
    {:status 200
     :body (slurp "./resources/public/oauth.html")}))
