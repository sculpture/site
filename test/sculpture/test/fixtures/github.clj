(ns sculpture.test.fixtures.github
  (:require
    [sculpture.db.yaml :as yaml]))

(defn github-api-mock [t]
  (with-redefs [sculpture.db.github/update-file!
                (fn [repo branch path opts])

                sculpture.db.github/fetch-paths-in-dir
                (fn [repo branch path]
                  ["/some-path"])

                sculpture.db.github/fetch-file
                (fn [repo branch path]
                  (yaml/to-string [{:id "123"
                                    :foo "bar"}
                                   {:id "456"
                                    :foo "bar"}]))

                sculpture.db.github/parse-file-content
                (fn [content]
                  content)]
    (t)))
