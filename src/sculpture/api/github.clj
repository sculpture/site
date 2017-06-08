(ns sculpture.api.github
  (:require
    [clojure.data.json :as json]
    [base64-clj.core :as base64]
    [environ.core :refer [env]]
    [org.httpkit.client :as http]))

(def github-auth-headers
  {"User-Agent" (env :github-api-user)
   "Authorization" (str "token " (env :github-api-token))})

(defonce path->sha (atom {}))

(defn update-file! [repo branch path {:keys [content message author committer]}]
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
    response))

(defn fetch-file [repo branch path]
  (let [file (-> @(http/get (str "https://api.github.com/repos/" repo "/contents/" path)
                            {:query-params {:ref branch}
                             :headers github-auth-headers})
                 :body
                 (json/read-str :key-fn keyword))]
    (swap! path->sha assoc (file :path) (file :sha))
    file))

(defn parse-file-content [file-response]
  (-> file-response
      :content
      (clojure.string/replace #"\n" "")
      (base64/decode "UTF-8")))

(defn fetch-paths-in-dir [repo branch path]
 (-> @(http/get (str "https://api.github.com/repos/" repo "/contents/" path)
                 {:query-params {:ref branch}
                  :headers github-auth-headers})
      :body
      (json/read-str :key-fn keyword)
      (->> (map :path))))


