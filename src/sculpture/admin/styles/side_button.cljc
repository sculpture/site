(ns sculpture.admin.styles.side-button
  (:require
    [garden.units :refer [px em rem]]
    [garden.arithmetic :as m]
    [sculpture.admin.styles.flat-button :refer [flat-button]]))

(defn side-button [icon side]
  (let [width (em 1.5)]
    [:&
     (flat-button icon)
     {:position "absolute"
      :top "4em"
      :width width
      :height "3em"
      :line-height "3em"}

     (case side
       :left {:left (m/- width)}
       :right {:right (m/- width)})]))
