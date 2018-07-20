(defproject sculpture "0.0.1"

  :source-paths ["src"]

  :dependencies [; COMMON
                 [org.clojure/clojure "1.9.0-alpha17"]

                 ; CLIENT
                 [org.clojure/clojurescript "1.9.562"]
                 [cljs-ajax "0.5.8" :exclusions [cheshire]]
                 [cljsjs/fuse "2.5.0-0"]
                 [garden "1.3.2"]
                 [com.andrewmcveigh/cljs-time "0.5.0"]
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]
                 [re-frame "0.9.1"]
                 [secretary "1.2.3"]
                 [venantius/accountant "0.1.7"]
                 [human-db/ui "0.0.2"]

                 ; SERVER
                 [environ "1.1.0"]
                 [hiccup "1.0.5"]
                 [commons-codec "1.10"]
                 [compojure "1.5.1"]
                 [http-kit "2.2.0"]
                 [javax.servlet/servlet-api "2.5"]
                 [ring-cors "0.1.8"]
                 [ring-middleware-format "0.7.0" :exclusions [cheshire]]

                 ; DB
                 [base64-clj "0.1.1"]
                 [org.clojure/data.json "0.2.6"]
                 [io.forward/yaml "1.0.6"]
                 [com.layerware/hugsql "0.4.7"]
                 [net.postgis/postgis-jdbc "2.2.1" :exclusions [org.postgresql/postgresql]]
                 [org.postgresql/postgresql "9.4.1208"]

                 ; DARKROOM
                 [me.raynes/conch "0.8.0"]
                 [me.raynes/fs "1.4.6"]
                 [clj-time "0.14.0"]
                 [amazonica "0.3.106"]
                 [cheshire "5.7.1"]]

  :main sculpture.server.core

  :plugins [[lein-figwheel "0.5.10"]
            [lein-environ "1.1.0"]
            [lein-cljsbuild "1.1.6"]]

  :figwheel {:server-port 3939
             :reload-clj-files {:clj false
                                :cljc true}}

  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["src"]
                        :figwheel     {:on-jsload "sculpture.admin.core/reload"}
                        :compiler     {:main       "sculpture.admin.core"
                                       :asset-path "/js/dev/out"
                                       :source-map true
                                       :output-to  "resources/public/js/sculpture.js"
                                       :output-dir "resources/public/js/dev/out"}}

                       {:id           "prod"
                        :source-paths ["src"]
                        :compiler     {:optimizations :advanced
                                       :main       "sculpture.admin.core"
                                       :asset-path "/js/prod/out"
                                       :output-to  "resources/public/js/sculpture.js"
                                       :output-dir "resources/public/js/prod/out"
                                       :externs ["resources/externs/leaflet.js"
                                                 "resources/externs/stackblur.js"]
                                       ; to debug advanced compilation issues, enable these options:
                                       ; :source-map "resources/public/js/sculpture.js.map"
                                       ; :pseudo-names true
                                       ; :pretty-print true
                                       }}]}

  :profiles {:test {:dependencies [[com.codeborne/phantomjsdriver "1.3.0"
                                    :exclusions [org.seleniumhq.selenium/selenium-java
                                                 org.seleniumhq.selenium/selenium-server
                                                 org.seleniumhq.selenium/selenium-remote-driver]]
                                   [org.seleniumhq.selenium/selenium-java "2.52.0"]
                                   [org.seleniumhq.selenium/selenium-remote-driver "2.52.0"]
                                   [clj-webdriver "0.7.2"]]}

             :uberjar {:aot :all
                       :prep-tasks ["compile" ["cljsbuild" "once" "prod"]]}})
