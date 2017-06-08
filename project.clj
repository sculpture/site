(defproject sculpture "0.0.1"

  :source-paths ["src"]

  :dependencies [; COMMON
                 [org.clojure/clojure "1.9.0-alpha17"]

                 ; CLIENT
                 [org.clojure/clojurescript "1.9.562"]
                 [cljs-ajax "0.5.8"]
                 [cljsjs/fuse "2.5.0-0"]
                 [garden "1.3.2"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]
                 [re-frame "0.9.1"]
                 [secretary "1.2.3"]
                 [venantius/accountant "0.1.7"]

                 ; SERVER
                 [environ "1.1.0"]
                 [hiccup "1.0.5"]

                 ; API
                 [base64-clj "0.1.1"]
                 [compojure "1.5.1"]
                 [http-kit "2.2.0"]
                 [io.forward/yaml "1.0.6"]
                 [javax.servlet/servlet-api "2.5"]
                 [org.clojure/data.json "0.2.6"]
                 [ring-cors "0.1.8"]
                 [ring-middleware-format "0.7.0"]]

  :main sculpture.core

  :plugins [[lein-figwheel "0.5.10"]
            [lein-environ "1.1.0"]]

  :figwheel {:server-port 3939
             :reload-clj-files {:clj false
                                :cljc true}}

  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["src"]
                        :figwheel     {:on-jsload "sculpture.admin.core/reload"}
                        :compiler     {:main       "sculpture.admin.core"
                                       :asset-path "/js/dev/out"
                                       :output-to  "resources/public/js/sculpture.js"
                                       :output-dir "resources/public/js/dev/out"}}

                       {:id           "prod"
                        :source-paths ["src"]
                        :compiler     {:optimizations :advanced
                                       :main       "sculpture.admin.core"
                                       :asset-path "/js/prod/out"
                                       :output-to  "resources/public/js/sculpture.js"
                                       :output-dir "resources/public/js/prod/out"}}]}

  :profiles {:test {:dependencies [[com.codeborne/phantomjsdriver "1.3.0"
                                    :exclusions [org.seleniumhq.selenium/selenium-java
                                                 org.seleniumhq.selenium/selenium-server
                                                 org.seleniumhq.selenium/selenium-remote-driver]]
                                   [org.seleniumhq.selenium/selenium-java "2.52.0"]
                                   [org.seleniumhq.selenium/selenium-remote-driver "2.52.0"]
                                   [clj-webdriver "0.7.2"]]}})
