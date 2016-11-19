(defproject sculpture "0.0.1"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.293"]
                 [re-frame "0.8.0"]]

  :plugins [[lein-figwheel "0.5.8"]]

  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["src"]
                        :figwheel     {:on-jsload "sculpture.core/reload"}
                        :compiler     {:main       "sculpture.core"
                                       :asset-path "/js/dev/out"
                                       :output-to  "resources/public/js/dev/sculpture.js"
                                       :output-dir "resources/public/js/dev/out"}}]}

  :profiles {:test {:dependencies [[com.codeborne/phantomjsdriver "1.3.0"
                                    :exclusions [org.seleniumhq.selenium/selenium-java
                                                 org.seleniumhq.selenium/selenium-server
                                                 org.seleniumhq.selenium/selenium-remote-driver]]
                                   [org.seleniumhq.selenium/selenium-java "2.52.0"]
                                   [org.seleniumhq.selenium/selenium-remote-driver "2.52.0"]
                                   [clj-webdriver "0.7.2"]]}})
