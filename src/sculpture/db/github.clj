(ns sculpture.db.github
  (:require
    [me.raynes.fs :as fs]
    [me.raynes.fs.compression :as fs.compression]
    [clojure.string :as string]
    [base64-clj.core :as base64]
    [environ.core :refer [env]]
    [org.httpkit.client :as http]
    [sculpture.json :as json]
    [clojure.java.io :as io])
  (:import
    [org.apache.commons.codec.digest DigestUtils]))

(defn git-sha [s]
  (DigestUtils/sha1Hex (str "blob " (count s) "\u0000" s)))

(def github-auth-headers
  {"User-Agent" (env :github-api-user)
   "Authorization" (str "token " (env :github-api-token))})

(defonce filename->sha (atom {}))

(defn fetch-file [repo branch path]
  (let [file (-> @(http/get (str "https://api.github.com/repos/" repo "/contents/" path)
                            {:query-params {:ref branch}
                             :headers github-auth-headers})
                 :body
                 json/decode)]
    (swap! filename->sha assoc (file :name) (file :sha))
    file))

(defn update-file! [repo branch path {:keys [content message author committer] :as opts}]
  (let [filename (last (string/split path #"/"))
        response (-> @(http/put (str "https://api.github.com/repos/" repo "/contents/" path)
                                {:headers (merge github-auth-headers
                                                 {"Content-Type" "application/json"})
                                 :body (json/encode
                                         {:branch branch
                                          :path path
                                          :message message
                                          :content (base64/encode content "UTF-8")
                                          :sha (@filename->sha filename)
                                          :committer committer
                                          :author author})})
                     :body
                     json/decode)]
    (if (get-in response [:content :name])
      (do
        (swap! filename->sha assoc
               (get-in response [:content :name])
               (get-in response [:content :sha]))
        true)
      (do
        (println "ERROR PUSHING FILE TO GITHUB" path)
        nil))))

(defn get-shas [files]
  (->> files
       (remove (fn [f]
                 (.isDirectory f)))
       (reduce (fn [memo file]
                 (assoc memo (.getName file) (git-sha (slurp file)))) {})))

(defn fetch-archive! [repo branch]
  (println "Fetching archive...")
  (let [temp-file (fs/temp-file "sculpture_data_archive_")
        temp-dir (fs/temp-dir "sculpture_data_archive_unpacked_")]
    (io/copy (:body @(http/get (str "https://api.github.com/repos/" repo "/zipball/" branch)
                       {:headers github-auth-headers
                        :as :byte-array}))
             temp-file)
    (println "Complete")
    (println "Unzipping archive...")
    (fs.compression/unzip temp-file temp-dir)
    (println "Complete")
    (reset! filename->sha (get-shas (file-seq temp-dir)))
    temp-dir))
