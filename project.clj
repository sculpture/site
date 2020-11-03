(defproject sculpture "0.0.1"

  :source-paths ["src"]

  :dependencies [; COMMON
                 [org.clojure/clojure "1.10.1"]
                 [io.bloomventures/commons "0.10.5"]
                 [metosin/malli "0.1.0"]

                 ; CLIENT
                 [org.clojure/clojurescript "1.10.764"]
                 [cljs-ajax "0.8.1"]
                 [cljsjs/fuse "2.5.0-0"]
                 [garden "1.3.10"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]
                 [reagent "0.10.0"]
                 [re-frame "1.1.1"]
                 [clj-commons/secretary "1.2.4"]
                 [venantius/accountant "0.2.5"]
                 [human-db/ui "0.0.3"]

                 ; SERVER
                 [environ "1.1.0"]
                 [hiccup "1.0.5"]
                 [commons-codec "1.10"]
                 [compojure "1.5.1"]
                 [http-kit "2.5.0"]
                 [javax.servlet/servlet-api "2.5"]
                 [ring-cors "0.1.8"]
                 [ring-middleware-format "0.7.0"]

                 ; DB
                 [base64-clj "0.1.1"]
                 [io.forward/yaml "1.0.6"]
                 [metosin/jsonista "0.2.7"]
                 [hikari-cp "2.13.0"]
                 [seancorfield/next.jdbc "1.1.588"]
                 [com.layerware/hugsql "0.5.1"]
                 [com.layerware/hugsql-adapter-next-jdbc "0.5.1"]
                 [org.postgresql/postgresql "42.2.16"]
                 [net.postgis/postgis-jdbc "2.5.0" :exclusions [org.postgresql/postgresql]]

                 ; DARKROOM
                 [clj-commons/conch "0.9.2"]
                 [clj-commons/fs "1.5.2"]
                 [clj-time "0.14.0"]
                 [amazonica "0.3.106"]]

  :main sculpture.server.core

  :plugins [[lein-figwheel "0.5.20"]
            [lein-environ "1.1.0"]
            [lein-cljsbuild "1.1.8"]]

  :figwheel {:server-port 3939
             :reload-clj-files {:clj false
                                :cljc true}}

  :cljsbuild {:builds {:dev {:source-paths ["src"]
                             :figwheel     {:on-jsload "sculpture.admin.core/reload"}
                             :compiler     {:main       "sculpture.admin.core"
                                            :asset-path "/js/dev/out"
                                            :source-map true
                                            :output-to  "resources/public/js/sculpture.js"
                                            :output-dir "resources/public/js/dev/out"}}

                       :prod {:source-paths ["src"]
                              :compiler     {:optimizations :advanced
                                             :closure-defines {goog.DEBUG false}
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
                                             }}}}

  :profiles {:test {:dependencies [[com.codeborne/phantomjsdriver "1.3.0"
                                    :exclusions [org.seleniumhq.selenium/selenium-java
                                                 org.seleniumhq.selenium/selenium-server
                                                 org.seleniumhq.selenium/selenium-remote-driver]]
                                   [org.seleniumhq.selenium/selenium-java "2.52.0"]
                                   [org.seleniumhq.selenium/selenium-remote-driver "2.52.0"]
                                   [clj-webdriver "0.7.2"]]}

             :uberjar {:aot :all
                       :prep-tasks ["compile" ["cljsbuild" "once" "prod"]]}})
