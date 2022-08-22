(defproject sculpture "0.0.1"

  :source-paths ["src"]

  :dependencies [; COMMON
                 [org.clojure/clojure "1.11.1"]
                 [io.bloomventures/commons "0.12.1"]
                 [io.bloomventures/omni "0.29.2"]
                 ;; [metosin/malli "0.1.0"] ;; from commons

                 ; CLIENT
                 [org.clojure/clojurescript "1.11.60"]
                 [cljs-ajax "0.8.4"] ;; in commons, but this is higher
                 [garden "1.3.10"] ;; in omni, but this is higher
                 ;; [com.andrewmcveigh/cljs-time "0.5.2"] ;; from commons
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]
                 [reagent "1.1.1"]
                 [re-frame "1.2.0"]
                 ;; [clj-commons/secretary "1.2.4"] in omni
                 ;; [venantius/accountant "0.2.5"] in commons
                 [human-db/ui "0.0.3"]

                 ; SERVER
                 ;; [hiccup "1.0.5"] in commons
                 [tada "0.2.2"]
                 [commons-codec "1.10"]
                 [http-kit "2.6.0"] ;; in omni, but this is higher
                 [ring-cors "0.1.8"]

                 ; DB
                 [base64-clj "0.1.1"]
                 [io.forward/yaml "1.0.10"]
                 [metosin/jsonista "0.2.7"]
                 [hikari-cp "2.13.0"]
                 [com.github.seancorfield/next.jdbc "1.2.761"]
                 [com.layerware/hugsql "0.5.1"]
                 [com.layerware/hugsql-adapter-next-jdbc "0.5.1"]
                 [org.postgresql/postgresql "42.4.2"]
                 ;; https://mvnrepository.com/artifact/net.postgis/postgis-jdbc
                 [net.postgis/postgis-jdbc "2.5.0" :exclusions [org.postgresql/postgresql]]

                 ; DARKROOM
                 [clj-commons/conch "0.9.2"]
                 [clj-commons/fs "1.5.2"]
                 [clj-time "0.14.0"]
                 [amazonica "0.3.157"]]

  :main sculpture.core

  :omni-config sculpture.core/omni-config

  :plugins [[io.bloomventures/omni "0.29.1"]]

  :profiles {:test {:dependencies [[com.codeborne/phantomjsdriver "1.3.0"
                                    :exclusions [org.seleniumhq.selenium/selenium-java
                                                 org.seleniumhq.selenium/selenium-server
                                                 org.seleniumhq.selenium/selenium-remote-driver]]
                                   [org.seleniumhq.selenium/selenium-java "2.52.0"]
                                   [org.seleniumhq.selenium/selenium-remote-driver "2.52.0"]
                                   [clj-webdriver "0.7.2"]]}

             :uberjar {:aot :all
                       :prep-tasks [["omni" "compile"]
                                    "compile" ]}})
