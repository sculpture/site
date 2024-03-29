(ns sculpture.darkroom.s3
  (:refer-clojure :exclude [list])
  (:require
    [amazonica.aws.s3 :as s3]
    [amazonica.aws.s3transfer :as s3-tx]
    [sculpture.config :refer [config]]))

(def creds
  {:access-key (:s3-access-key config)
   :secret-key (:s3-secret-key config)
   :endpoint (:s3-endpoint config)})

(def bucket (:s3-bucket config))

(defn object-exists? [path]
  (try
    (s3/get-object-metadata creds :bucket-name bucket :key path)
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

(defn download!
  [path dir]
  (println "Downloading..." path)
  (let [file (str dir "/" path)
        prom (promise)
        download (s3-tx/download creds
                                 bucket
                                 path
                                 file)
        listener (fn [{:keys [event bytes-transferred]}]
                   (when (= :completed event)
                     (println "Complete")
                     (deliver prom (clojure.java.io/file file))))]
    ((:add-progress-listener download) listener)
    prom))

(defn list [prefix]
  (-> (s3/list-objects creds
        :bucket-name bucket
        :prefix prefix)
      :object-summaries))

