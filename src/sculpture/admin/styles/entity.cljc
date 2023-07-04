(ns sculpture.admin.styles.entity
  (:require
   [garden.units :refer [px em rem]]
   [sculpture.admin.styles.side-button :refer [side-button]]
   [sculpture.admin.styles.fontawesome :refer [fontawesome]]
   [sculpture.admin.styles.colors :as colors]))

(def entity-styles
  [
   [:>button.edit
    {:z-index 1000}
    (side-button \uf040 :right)]

   [:>.entity
    {:overflow-y "auto"}

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

      [:&.captured-at:before (fontawesome \uf133)]
      [:&.category:before (fontawesome \uf02c)]
      [:&.city:before (fontawesome \uf276)]
      [:&.colors:before (fontawesome \uf1fb)]
      [:&.commission:before (fontawesome \uf0e3)]
      [:&.dimensions:before (fontawesome \uf0b2)]
      [:&.gender:before (fontawesome \uf22d)]
      [:&.location:before (fontawesome \uf041)]
      [:&.materials:before (fontawesome \uf12e)]
      [:&.nationalities:before (fontawesome \uf0ac)]
      [:&.nearby:before (fontawesome \uf277)]
      [:&.note:before (fontawesome \uf249)]
      [:&.regions:before (fontawesome \uf279)]
      [:&.sculpture:before (fontawesome \uf27c)]
      [:&.segments:before (fontawesome \uf27c)]
      [:&.tags:before (fontawesome \uf02c)]
      [:&.user:before (fontawesome \uf007)]
      [:&.website:before (fontawesome \uf0c1)]
      [:&.wikipedia:before (fontawesome \uf266)]

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
