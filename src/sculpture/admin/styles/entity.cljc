(ns sculpture.admin.styles.entity
  (:require
    [garden.units :refer [px em rem]]
    [sculpture.admin.styles.side-button :refer [side-button]]
    [sculpture.admin.styles.fontawesome :refer [fontawesome]]
    [sculpture.admin.styles.colors :as colors]))

(def entity-styles
  [:>.active-entity

   [:>button.edit
    (side-button \uf040 :right)]

   [:>.entity

    [:>.info
     {:padding "1.5em"
      :background colors/accent-color
      :color "white"}

     [:a
      {:color "white"
       :text-decoration "none"}

      [:&:hover
       {:text-decoration "underline"}]]

     [:>h1
      {:font-size "1.25em"}]

     [:>h2
      {:font-size "1em"
       :font-weight "normal"
       :display "flex"
       :justify-content "space-between"}]]

    [:>.meta
     {:padding "1.5em"}

     [:>.row
      {:margin-bottom "0.75em"
       :color colors/text-color}

      [:&:last-child
       {:margin-bottom 0}]

      [:&:before
       {:width "1.5em"
        :color "#000"
        :text-align "center"
        :display "inline-block"
        :vertical-align "middle"
        :margin-right "0.75em"}]

      [:a
       {:color colors/text-color
        :text-decoration "none"
        :vertical-align "middle"}

       [:&:hover
        {:text-decoration "underline"}]]

      [:&.tags:before (fontawesome \uf02c)]
      [:&.materials:before (fontawesome \uf12e)]
      [:&.location:before (fontawesome \uf041)]
      [:&.regions:before (fontawesome \uf041)]
      [:&.nearby:before (fontawesome \uf041)]
      [:&.commission:before (fontawesome \uf0e3)]
      [:&.note:before (fontawesome \uf249)]
      [:&.captured-at:before (fontawesome \uf133)]
      [:&.user:before (fontawesome \uf007)]
      [:&.sculpture:before (fontawesome \uf27c)]
      [:&.gender:before (fontawesome \uf22d)]
      [:&.website:before (fontawesome \uf0c1)]
      [:&.wikipedia:before (fontawesome \uf266)]
      [:&.dimensions:before (fontawesome \uf0b2)]
      [:&.colors:before (fontawesome \uf1fb)]

      [:&.colors
       [:>.color
        {:margin-right "0.5em"}

        [:>.swatch
         {:border-radius "2px"
          :margin-right "2px"
          :vertical-align "middle"}]

        [:>.name
         {:display "inline-block"
          :vertical-align "middle"}]]]

      [:>div
       {:display "inline-block"
        :vertical-align "middle"}]]]

    [:>.related
     [:>h2
      {:font-size "1em"
       :margin-left (rem 0.75)
       :padding [[(rem 0.75) 0]]}]]]])
