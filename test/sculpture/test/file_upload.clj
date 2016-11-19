(ns sculpture.test.file-upload
  (:require
    [clojure.test :refer [deftest testing is]]
    [clj-webdriver.taxi :as taxi]))

(taxi/set-driver! {:browser :phantomjs})

(deftest hello-world
  (taxi/to "http://localhost:3449")
  (let [file-input (taxi/find-element {:tag :input
                                       :type "file"
                                       :multiple ""})]
    (is file-input)))
