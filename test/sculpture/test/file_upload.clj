(ns sculpture.test.file-upload
  (:require
    [clojure.test :refer [deftest testing is]]
    [clj-webdriver.taxi :as taxi]))

(deftest hello-world
  (taxi/set-driver! {:browser :phantomjs})
  (taxi/to "http://localhost:3449")
  (is (taxi/find-element {:text "Hello World!"})))
