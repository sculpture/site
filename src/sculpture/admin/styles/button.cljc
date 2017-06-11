(ns sculpture.admin.styles.button)

(defn button []
  [:&
   {:display "inline-block"
    :border "1px solid #ccc"
    :border-radius "3px"
    :padding [[0 "0.25em"]]
    :text-transform "uppercase"
    :font-size "0.7em"
    :line-height "1.7em"
    :height "1.5em"
    :text-decoration "none"
    :color "#aaa"}

   [:&:hover
    {:color "#888"
     :border-color "#aaa"}]

   [:&:active
    {:color "#666"
     :border-color "#888"}]])
