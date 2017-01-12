(ns sculpture.admin.styles.app
  (:require
    [garden.core :refer [css]]
    [garden.units :refer [px em rem]]
    [garden.arithmetic :as m]))

(def search-styles
  (let [border-width (px 0)
        pad (rem 0.75)]

    [:.search
     {:width "30%"
      :max-height "100vh"
      :min-width "15em"
      :background-color "#fff"
      :box-shadow "0 0 2px 0 #ccc"
      :margin [[0 "1em"]]
      :display "flex"
      :flex-direction "column"}

     [:.query
      {:padding pad
       :background "#53acf1"}

      [:input
       {:height (rem 2)
        :border-radius "3px"
        :border "none"
        :padding [[0 (rem 0.5)]]
        :line-height (rem 2)
        :font-size "1.1em"
        :width "100%"
        :box-sizing "border-box"}]]

     [:.results
      {:overflow-x "hidden"
       :overflow-y "scroll"}

      [:.group

       [:h2
        {:font-size "1em"
         :margin-left pad
         :padding [[pad 0]]}]

       (let [height (rem 2.6)]
         [:a.result
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

          [:img
           {:width height
            :height height
            :margin-right (rem 0.5)
            :background "#CCC"
            :float "left"}]

          [:.h1 :.h2
           {:font-size "0.9em"
            :height (m// height 2)
            :line-height (m// height 2)}]

          [:.h1
           {:font-weight "bold"
            :color "#000"
            :white-space "nowrap"}]

          [:.h2
           {:color "#AAA"}]])]]]))

(def app-styles
  [:.app
   {:display "flex"}

   search-styles

   [:.entity
    {:width "70%"}]])

(def reset-styles
  [:body
   {:margin 0
    :padding 0}
   [:h1 :h2 :h3
    {:margin 0}]])

(def styles
  (css
    [:body
     {:font-size "14px"
      :font-family "Roboto, Arial"
      :line-height "1.35"
      :background "#ededed"}]
    reset-styles
    app-styles))


