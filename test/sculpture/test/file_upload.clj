(ns sculpture.test.file-upload
  (:require
    [clojure.test :refer :all]
    [clj-webdriver.taxi :as taxi]
    [sculpture.test.helpers :as helpers]))

(use-fixtures :once helpers/phantomjs-driver-fixture)

(deftest uploading-photos
  (testing "when page loads"
    (taxi/to "http://localhost:3939")
    (testing "then input exists"
      (let [file-input (taxi/find-element {:tag :input
                                           :type "file"
                                           :multiple ""})]
        (is file-input)

        (testing "when uploading file"
          (spit "/tmp/photo.png" "...")
          (helpers/upload-file "/tmp/photo.png")
          (taxi/take-screenshot :file "./shoy.png")
         (testing "then show file name on page"
            (is (taxi/find-element {:tag :div
                                    :text "photo.png"}))))
        (testing "when uploading multiple files"
          (spit "/tmp/photo1.png" "...")
          (spit "/tmp/photo2.png" "..."))))))
