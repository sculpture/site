(ns sculpture.admin.styles.button
  (:require
    [sculpture.admin.styles.fontawesome :refer [fontawesome]]
    [sculpture.admin.styles.colors :as colors]))

(defn button [mode]
  [:&
   {:display "inline-block"
    :border-radius "3px"
    :padding [[0 "0.5em"]]
    :text-transform "uppercase"
    :font-size "0.75rem"
    :line-height "1.5rem"
    :height "1.5rem"
    :text-decoration "none"
    :cursor "pointer"}

   (case mode
     :primary
     [:&
      {:border "none"
       :color colors/accent-color
       :background "white"}

      [:&:hover
       {:background "rgba(255,255,255,0.8)"}]

      [:&:active
       {:background "rgba(255,255,255,0.6)"}]]

     :secondary
     [:&
      {:border "1px solid white"
       :color "white"
       :background colors/accent-color}

      [:&:hover
       {:color "rgba(255,255,255,0.8)"
        :border-color "rgba(255,255,255,0.8)"}]

      [:&:active
       {:color "rgba(255,255,255,0.6)"
        :border-color "rgba(255,255,255,0.6)"}]])])


(defn tiny-button [icon]
  [:&
   {:color "#ccc"
    :background "none"
    :font-size "1em"
    :padding 0
    :border "none"
    :cursor "pointer"}

   [:&:before
    (let [icon (case icon
                 :+ \uf055
                 :x \uf057)]
      (fontawesome icon))]

   [:&:hover
    {:color colors/accent-color}]

   [:&:active
    {}]])
