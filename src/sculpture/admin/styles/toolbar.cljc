(ns sculpture.admin.styles.toolbar
  (:require
    [sculpture.admin.styles.flat-button :refer [flat-button]]))

(def toolbar-styles
  (let [height "2rem"]
    [:>.toolbar
     {:position "absolute"
      :top "0.75rem"
      :right 0
      :z-index 1000
      :height height}

     [:>button.new
      (flat-button \uf067)
      {:height height
       :width height
       :vertical-align "top"}]

     [:>.user
      {:height "100%"
       :display "inline-block"}

      [:>img.avatar
       {:height "100%"
        :display "inline-block"}]]]))
