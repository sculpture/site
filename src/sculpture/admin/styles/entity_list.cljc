(ns sculpture.admin.styles.entity-list
  (:require
    [garden.arithmetic :as m]
    [garden.units :refer [px em rem]]))

(def entity-list-styles
  (let [pad (rem 0.75)
        height (rem 2.6)]
    [:.entity-list
     {:overflow-x "hidden"
      :overflow-y "scroll"}

     [:>.entity
      {:display "block"
       :clear "both"
       :text-decoration "none"
       :padding [[(m// pad 2) pad]]
       :height height
       :position "relative"}

      [:&:hover
       {:background "#f3f3f3"}
       ["&::after"
        {:content "\"»\""
         :position "absolute"
         :right pad
         :top (m// pad 2)
         :color "#ccc"
         :font-size (m// height 2)
         :height height
         :line-height height}]]

      [:&:active
       {:background "#eee"}
       ["&::after"
        {:color "#aaa"}]]

      [:>.photo
       {:margin-right (rem 0.5)
        :background "#CCC"
        :float "left"
        :width height
        :height height
        :overflow "hidden"}

       [:>.image

        [:>img
         {:width height
          :height height}]]]

      [:>.h1
       :>.h2
       {:font-size "0.9em"
        :height (m// height 2)
        :line-height (m// height 2)}]

      [:>.h1
       {:font-weight "bold"
        :color "#000"
        :white-space "nowrap"}]

      [:>.h2
       {:color "#AAA"}]]]))
