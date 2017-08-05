(ns sculpture.db.github
  (:require
    [me.raynes.fs :as fs]
    [me.raynes.fs.compression :as fs.compression]
    [clojure.data.json :as json]
    [base64-clj.core :as base64]
    [environ.core :refer [env]]
    [org.httpkit.client :as http]))

(def github-auth-headers
  {"User-Agent" (env :github-api-user)
   "Authorization" (str "token " (env :github-api-token))})

(defonce path->sha (atom {}))

(defn fetch-file [repo branch path]
  (let [file (-> @(http/get (str "https://api.github.com/repos/" repo "/contents/" path)
                            {:query-params {:ref branch}
                             :headers github-auth-headers})
                 :body
                 (json/read-str :key-fn keyword))]
    (swap! path->sha assoc (file :path) (file :sha))
    file))

(defn update-file! [repo branch path {:keys [content message author committer] :as opts}]
  (if (nil? (@path->sha path))
    (do
      (fetch-file repo branch path)
      (update-file! repo branch path opts))
    (let [response (-> @(http/put (str "https://api.github.com/repos/" repo "/contents/" path)
                                  {:headers (merge github-auth-headers
                                                   {"Content-Type" "application/json"})
                                   :body (json/write-str
                                           {:branch branch
                                            :path path
                                            :message message
                                            :content (base64/encode content "UTF-8")
                                            :sha (@path->sha path)
                                            :committer committer
                                            :author author})})
                       :body
                       (json/read-str :key-fn keyword))]
      (swap! path->sha assoc
             (get-in response [:content :path])
             (get-in response [:content :sha]))
      response)))

(defn fetch-archive [repo branch]
  (println "Fetching archive...")
  (let [temp-file (fs/temp-file "sculpture_data_archive_")
        temp-dir (fs/temp-dir "sculpture_data_archive_unpacked_")]
    (clojure.java.io/copy (:body @(http/get (str "https://api.github.com/repos/" repo "/zipball/" branch)
                                    {:headers github-auth-headers
                                     :as :byte-array}))
                          temp-file)
    (println "Complete")
    (println "Unzipping archive...")
    (fs.compression/unzip temp-file temp-dir)
    (println "Complete")
    temp-dir))
