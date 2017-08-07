(ns sculpture.darkroom.util
  (:require
    [clojure.string :as string]
    [me.raynes.fs :as fs]
    [sculpture.darkroom.s3 :as s3]
    [sculpture.darkroom.convert :as convert]))

(def cache-path "image_cache")

(defn download-image [])

(defn fetch!
  "Return image file from local filesytem cache, download from S3 if necessary"
  [key]
  (let [path (str cache-path "/" key)]
    (if (fs/exists? path)
      (clojure.java.io/file path)
      @(s3/download! key cache-path))))

(defn download-all-to-cache! [prefix]
  (doseq [key (map :key (s3/list prefix))]
    (fetch! key)))

(defn add-preview-image! []
  (doseq [key (->> (s3/list "original/")
                   (map :key)
                   (filter (fn [key]
                             (string/ends-with? key ".jpg"))))]
    (println "Processing..." key)
    (let [new-path (string/replace key "original" "preview")]
      (if (s3/object-exists? new-path)
        (println "Exists. Skipping.")
        (-> (fetch! key)
            (convert/convert! {:maxsize 300 :sharpen true})
            (s3/upload! new-path))))))

