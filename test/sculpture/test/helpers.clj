(ns sculpture.test.helpers
  (:require
    [clj-webdriver.driver :refer [init-driver]]
    [clj-webdriver.taxi :as taxi])
  (:import
    [org.openqa.selenium.phantomjs PhantomJSDriver]
    [org.openqa.selenium.remote DesiredCapabilities]))

(def driver (atom nil))

(defn phantomjs-driver-fixture [t]
  (reset! driver (init-driver
                   {:webdriver
                    (PhantomJSDriver. (doto (DesiredCapabilities.)
                                        (.setCapability "phantomjs.cli.args" (into-array String ["--ignore-ssl-errors=true"
                                                                                                  "--webdriver-loglevel=WARN"]))))}))
  (taxi/set-driver! @driver)
  (t)
  (taxi/quit @driver))

(defn upload-file [path]
  ; must pass empty object-array b/c clojure can't deal with var-args yet
  ; http://dev.clojure.org/jira/browse/CLJ-440
  (.. (:webdriver @driver) (executePhantomJS (str "this.uploadFile('input[type=file]', '" path "');") (object-array []))))
