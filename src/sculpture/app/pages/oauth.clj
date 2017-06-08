(ns sculpture.app.pages.oauth
  (:require
    [hiccup.core :as hiccup]))

(defn html []
  (hiccup/html
    [:html
     [:body
      [:script
       "var token = window.location.toString().match(/access_token=(.*)&/, '')[1];"
       "window.opener.postMessage(token, window.location);"
       "window.close();"]]]))
