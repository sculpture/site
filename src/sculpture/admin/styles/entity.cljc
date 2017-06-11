(ns sculpture.admin.styles.entity
  (:require
    [garden.units :refer [px em rem]]
    [sculpture.admin.styles.side-button :refer [side-button]]
    [sculpture.admin.styles.fontawesome :refer [fontawesome]]
    [sculpture.admin.styles.colors :refer [accent-color secondary-color]]))

(def entity-styles
  [:>.active-entity

   [:>button.edit
    (side-button \uf040 :right)]

   [:>.entity

    [:>.info
     {:padding "1.5em"
      :background accent-color
      :color "white"}

     [:>a
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
      {:margin-bottom "0.75em"}

      [:&:last-child
       {:margin-bottom 0}]

      [:&:before
       {:width "1em"
        :text-align "center"
        :display "inline-block"
        :margin-right "0.75em"}]

      [:&.tags:before (fontawesome \uf02c)]
      [:&.materials:before (fontawesome \uf0e3)]
      [:&.location:before (fontawesome \uf041)]
      [:&.commission:before (fontawesome \uf0e3)]
      [:&.note:before (fontawesome \uf249)]
      [:&.captured-at:before (fontawesome \uf133)]
      [:&.user:before (fontawesome \uf007)]
      [:&.sculpture:before (fontawesome \uf03e)]
      [:&.gender:before (fontawesome \uf22d)]
      [:&.website:before (fontawesome \uf0c1)]
      [:&.wikipedia:before (fontawesome \uf266)]

      [:>div
       {:display "inline-block"}]]]

    [:>.related
     [:>h2
      {:font-size "1em"
       :margin-left (rem 0.75)
       :padding [[(rem 0.75) 0]]}]]]])
