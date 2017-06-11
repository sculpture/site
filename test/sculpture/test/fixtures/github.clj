(ns sculpture.test.fixtures.github
  (:require
    [sculpture.server.yaml :as yaml]))

(defn github-api-mock [t]
  (with-redefs [sculpture.server.github/update-file!
                (fn [repo branch path opts])

                sculpture.server.github/fetch-paths-in-dir
                (fn [repo branch path]
                  ["/some-path"])

                sculpture.server.github/fetch-file
                (fn [repo branch path]
                  (yaml/to-string [{:id "123"
                                    :foo "bar"}
                                   {:id "456"
                                    :foo "bar"}]))

                sculpture.server.github/parse-file-content
                (fn [content]
                  content)]
    (t)))
