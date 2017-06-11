(ns sculpture.admin.styles.editor
  (:require
    [sculpture.admin.styles.button :refer [button]]))

(def editor-styles
  [:>.edit
   {:position "relative"}

   [:a.button
    {:position "absolute"
     :top 0
     :right 0}
    (button)]

   [:.invalid
    {:background "red"}]])
