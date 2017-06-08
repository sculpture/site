(ns sculpture.test.fixtures.github
  (:require
    [sculpture.api.yaml :as yaml]))

(defn github-api-mock [t]
  (with-redefs [sculpture.api.github/update-file!
                (fn [repo branch path opts])

                sculpture.api.github/fetch-paths-in-dir
                (fn [repo branch path]
                  ["/some-path"])

                sculpture.api.github/fetch-file
                (fn [repo branch path]
                  (yaml/to-string [{:id "123"
                                    :foo "bar"}
                                   {:id "456"
                                    :foo "bar"}]))

                sculpture.api.github/parse-file-content
                (fn [content]
                  content)]
    (t)))
