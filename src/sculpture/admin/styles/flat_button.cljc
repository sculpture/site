(ns sculpture.admin.styles.flat-button
  (:require
    [sculpture.admin.styles.fontawesome :refer [fontawesome]]
    [sculpture.admin.styles.colors :refer [accent-color]]))

(defn flat-button [icon]
  [:&
   {:background accent-color
    :display "inline-block"
    :text-decoration "none"
    :text-align "center"
    :border "none"
    :cursor "pointer"
    :color "white"
    :font-size "1em"
    :padding 0}

   [:&:before
    (fontawesome icon)]

   [:&:hover
    {:background "#3ea2ef"}]])
