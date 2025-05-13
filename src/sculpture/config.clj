(ns sculpture.config
  (:require
    [bloom.commons.config :as config]))

(def config
  (config/read
   "config.edn"
   [:map
    [:http-port :int]
    [:environment [:enum :dev :prod]]
    [:cookie-secret :string]
    [:db-url [:re #"^postgresql://.*$"]]
    [:mapbox-token :string]
    [:mapquest-api-key :string]
    [:google-client-id :string]
    [:google-maps-api-key :string]
    [:oauth-redirect-uri :string]
    [:data-dir :string]
    [:github-api-user :string]
    [:github-api-token :string]
    [:github-repo :string]
    [:github-repo-branch :string]
    [:github-committer-name :string]
    [:github-committer-email :string]
    [:s3-access-key :string]
    [:s3-secret-key :string]
    [:s3-endpoint :string]
    [:s3-bucket :string]]))
