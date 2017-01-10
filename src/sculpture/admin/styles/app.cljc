(ns sculpture.admin.styles.app
  (:require
    [garden.core :refer [css]]))

(def app-styles
  [:.app
   {:display "flex"}

   [:.search
    {:width "30%"
     :min-width "15em"}

    [:.group

     [:h2
      {:font-size "1em"
       :margin [["0.5em" 0]]}]

     [:a.result
      {:display "block"
       :text-decoration "none"
       :margin [["0.5em" 0]]}

      [:.sculpture
       {:clear "both"}

       [:img
        {:width "2em"
         :height "2em"
         :margin-right "0.5em"
         :float "left"}]

       [:.title
        {:font-weight "bold"
         :color "#000"
         :white-space "nowrap"}]

       [:.artist
        {:color "#999"}]]]]]

   [:.entity
    {:width "70%"}]])

(def styles
  (css
    app-styles))


