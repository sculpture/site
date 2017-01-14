(ns sculpture.handler)

(defn force-index [_]
  {:status 200
   :body (slurp "./resources/public/index.html")})
