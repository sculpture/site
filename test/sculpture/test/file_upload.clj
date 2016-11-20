(ns sculpture.test.file-upload
  (:require
    [clojure.test :refer [deftest testing is]]
    [clj-webdriver.taxi :as taxi])
  (:import
    [org.openqa.selenium.phantomjs.PhantomJSDriver]))


(def driver
  (taxi/set-driver! {:browser :phantomjs}))

(deftest uploading-photos
  (testing "when page loads"
    (taxi/to "http://localhost:3449")
    (testing "then input exists"
      (let [file-input (taxi/find-element {:tag :input
                                           :type "file"
                                           :multiple ""})]
        (is file-input)

        (testing "when uploading file"
          (spit "/tmp/photo.png" "...")
          (taxi/execute-script "document.getElementsByTagName('input')[0].removeAttribute('multiple')")
          (taxi/send-keys file-input "/tmp/photo.png")
          ;(println "Driver" (.executePhantomJS (:webdriver driver) "console.log('hello')"))
          (println "Driver" (type (:webdriver driver)))
         (testing "then show file name on page"
            (is (taxi/find-element {:tag :div
                                    :text "C:\\fakepath\\photo.png"}))))
        (testing "when uploading multiple files"
          (spit "/tmp/photo1.png" "...")
          (spit "/tmp/photo2.png" "..."))))))
