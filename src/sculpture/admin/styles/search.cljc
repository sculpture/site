(ns sculpture.admin.styles.search
  (:require
    [garden.arithmetic :as m]
    [garden.units :refer [px em rem]]
    [sculpture.admin.styles.fontawesome :refer [fontawesome]]
    [sculpture.admin.styles.colors :refer [accent-color secondary-color]]))

(def pad (rem 0.75))

(def search-query-styles
  (let [input-pad (rem 0.5)]
    [:>.query
     {:padding pad
      :background accent-color}

     [:>input
      {:height (rem 2)
       :border-radius "3px"
       :border "none"
       :padding [[0 input-pad]]
       :line-height (rem 2)
       :font-size "1.1em"
       :width "100%"
       :box-sizing "border-box"}]

     [:>button.clear
      {:position "absolute"
       :top (m/+ pad input-pad)
       :right (m/+ pad input-pad)
       :background "none"
       :border "none"
       :color secondary-color
       :cursor "pointer"}

      [:&:hover
       {:color accent-color}]

      [:&:before
       (fontawesome \uf057)]]]))

(def search-result-styles
  [:>.results
   {:overflow-y "auto"}

   [:>.no-results
    {:padding [[pad]]}]

   [:>.group

    [:>h2
     {:font-size "1em"
      :margin-left pad
      :padding [[pad 0]]}]]])
