(ns sculpture.env
  (:require-macros
    [sculpture.env :refer [fetch-from-env]]))

(def env
  {:mapbox-token (fetch-from-env :mapbox-token)
   :google-client-id (fetch-from-env :google-client-id)
   :oauth-redirect-uri (fetch-from-env :oauth-redirect-uri)})


