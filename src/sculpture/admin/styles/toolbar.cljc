(ns sculpture.admin.styles.toolbar
  (:require
    [sculpture.admin.styles.flat-button :refer [flat-button]]
    [sculpture.admin.styles.colors :refer [accent-color]]))

(def toolbar-styles
  (let [height "2rem"]
    [:>.toolbar
     {:position "absolute"
      :top "0.75rem"
      :right 0
      :z-index 1000
      :height height
      :background accent-color
      :color "white"}

     [:>button.auth
      (flat-button \uf234)
      {:height height
       :width height
       :vertical-align "top"
       :-webkit-font-smoothing "antialiased"}]

     [:>button.menu
      (flat-button \uf0c9)
      {:height height
       :width height
       :vertical-align "top"}]

     [:>img.avatar
      {:height "100%"
       :display "inline-block"}]]))
