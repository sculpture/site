(ns sculpture.darkroom.s3
  (:require
    [amazonica.aws.s3 :as s3]
    [amazonica.aws.s3transfer :as s3-tx]
    [environ.core :refer [env]]))

(def creds
  {:access-key (env :s3-access-key)
   :secret-key (env :s3-secret-key)
   :endpoint (env :s3-endpoint)})

(def bucket (env :s3-bucket))

(defn object-exists? [path]
  (try
    (s3/get-object-metadata creds :bucket-name (env :s3-bucket) :key path)
    (catch Exception e nil)))

(defn upload!
  "given a file, uploads it to s3 as path"
  [file path]
  (println "Uploading..." (.getName file) "to" path)

  (s3/put-object creds
                 :bucket-name bucket
                 :key path
                 :file file))

(defn upload-if-empty! [file path]
  (if (object-exists? path)
    (println "Skipped Upload for..." (.getName file))
    (upload! file path)))

(defn delete!
  [path]
  (println "Deleting..." path)
  (s3/delete-object creds
                    :bucket-name bucket
                    :key path))
