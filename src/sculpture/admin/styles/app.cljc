(ns sculpture.admin.styles.app
  (:require
    [garden.core :refer [css]]
    [garden.units :refer [px em rem]]
    [garden.arithmetic :as m]))

(defn floating-box []
  {:background "#fff"
   :box-shadow "0 0 2px 0 #ccc"})

(defn button []
  [:&
   {:display "inline-block"
    :border "1px solid #ccc"
    :border-radius "3px"
    :padding [[0 "0.25em"]]
    :text-transform "uppercase"
    :font-size "0.7em"
    :line-height "1.7em"
    :height "1.5em"
    :text-decoration "none"
    :color "#aaa"}

   [:&:hover
    {:color "#888"
     :border-color "#aaa"}]

   [:&:active
    {:color "#666"
     :border-color "#888"}]])

(def entity-styles
  [:>.entity
   {:padding "2em"
    :position "relative"}

   [:a.edit
    (button)
    {:position "absolute"
     :top "1em"
     :right "1em"}]

   [:a.back
    (button)
    {:position "absolute"
     :top "1em"
     :left "1em"}]

   [:.sculptures
    {:display "flex"
     :flex-wrap "wrap"}

    (let [size "100px"]

      [:.sculpture
       {:display "inline-block"
        :border "1px solid #ededed"
        :width size
        :padding "0.5em"
        :margin "0.5em"
        :text-align "center"
        :text-decoration "none"}

       [:.photo
        {:width size
         :height size
         :display "flex"
         :align-items "center"
         :justify-content "center"
         :margin-bottom "0.5em"}

        [:img
         {:max-width size
          :max-height size
          :vertical-align "center"
          :text-align "center"}]]

       [:.title
        {:font-weight "bold"
         :color "#000"}]

       [:.year
        {:color "#AAA"}]

       [:&:hover
        {:border-color "#ccc"}]

       [:&:active
        {:border-color "#aaa"}]])]])

(def search-styles
  (let [border-width (px 0)
        pad (rem 0.75)
        input-pad (rem 0.5)]

    [:>.search
     {:display "flex"
      :flex-direction "column"}

     [:.query
      {:padding pad
       :background "#53acf1"}

      [:input
       {:height (rem 2)
        :border-radius "3px"
        :border "none"
        :padding [[0 input-pad]]
        :line-height (rem 2)
        :font-size "1.1em"
        :width "100%"
        :box-sizing "border-box"}]

      [:button.clear
       {:position "absolute"
        :top (m/+ pad input-pad)
        :right (m/+ pad input-pad)}]]

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

(def entity-edit-styles
  [:>.edit
   {:position "relative"}
   [:a.button
    {:position "absolute"
     :top 0
     :right 0}
    (button)]])

(def app-styles
  [:.app
   [:>.mega-map
    {:width "100vw"
     :height "100vh"}]

   [:>button.new
    {:position "absolute"
     :top 0
     :right 0
     :z-index 1000}]

   [:>.sidebar
    {:position "absolute"
     :left "1.5em"
     :top 0
     :min-width "15em"
     :width "30%"
     :max-height "100vh"
     :z-index 1000
     :overflow-y "scroll"}
    (floating-box)

    search-styles
    entity-styles]

  [:>.main
   {:position "absolute"
    :top 0
    :right "1.5em"
    :z-index 1000
    :padding "2em"
    :max-height "100vh"
    :overflow-y "scroll"
    :box-sizing "border-box"}
   (floating-box)

   entity-edit-styles]])

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


