(ns sculpture.test.file-upload
  (:require
    [clojure.test :refer [deftest testing is]]
    [clj-webdriver.taxi :as taxi]))

(deftest hello-world
  (taxi/set-driver! {:browser :phantomjs})
  (taxi/to "http://localhost:3449")
  (let [file-input (taxi/find-element {:tag :input
                                       :type "file"
                                       :multiple ""})]
    (is file-input)))
