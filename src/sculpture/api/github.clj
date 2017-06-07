(ns sculpture.api.github
  (:require
    [clojure.data.json :as json]
    [base64-clj.core :as base64]
    [environ.core :refer [env]]
    [org.httpkit.client :as http]))

(def api-user (env :github-api-user))
(def api-token (env :github-api-token))

(defn fetch-file [repo path]
  (let [file (-> @(http/get (str "https://api.github.com/repos/" repo "/contents/" path)
                            {:query-params {:ref "test"}
                             :headers {"User-Agent" api-user
                                       "Authorization" (str "token " api-token)}})
                 :body
                 (json/read-str :key-fn keyword))]
    file))

(defn parse-file-content [file-response]
  (-> file-response
      :content
      (clojure.string/replace #"\n" "")
      (base64/decode "UTF-8")))

(defn fetch-paths-in-dir [repo path]
 (-> @(http/get (str "https://api.github.com/repos/" repo "/contents/" path)
                 {:headers {"User-Agent" api-user
                            "Authorization" (str "token " api-token)}})
      :body
      (json/read-str :key-fn keyword)
      (->> (map :path))))


