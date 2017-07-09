(ns sculpture.admin.styles.editor
  (:require
    [sculpture.admin.styles.colors :as colors]
    [sculpture.admin.styles.button :refer [button tiny-button]]))

(def editor-styles
  [:>.edit
   {:position "relative"}

   [:>table
    {:width "100%"
     :border-collapse "collapse"}

    [:>tbody
     [:>tr

      ["&:nth-child(even)"
       {:background "#f1f6fb"}]

      [:&.invalid
       {:background "red"}]

      [:>td
       {:padding "0.5em"}

       [:>button.delete
        (tiny-button :x)]

       [:>textarea
        :>input
        {:padding "0.25em"
         :font-size "1em"
         :margin "-0.25em 0"
         :min-width "20em"}]

       [:>select
        {:font-size "1em"}]

       [:>.lookup

        [:>.value
         [:>button.remove
          {:margin-left "0.25em"}
          (tiny-button :x)]

         [:>.related
          {:display "inline-block"}]]

        [:>.search

         [:>input]

         [:>.results
          [:>.result]]

         [:>button.cancel
          {:margin-left "0.25em"}
          (tiny-button :x)]]

        [:>button.plus
         (tiny-button :+)]]

       [:&.key
        {:text-align "right"
         :vertical-align "top"}]]]]]])
