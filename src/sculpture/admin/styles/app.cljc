(ns sculpture.admin.styles.app
  (:require
    [garden.core :refer [css]]
    [garden.units :refer [px em rem]]
    [garden.arithmetic :as m]))

(def accent-color "rgba(83,172,241,1)")
(def secondary-color "#CCC")

(defn floating-box []
  {:background "#fff"
   :box-shadow "0 0 2px 0 #ccc"})

(defn fontawesome [unicode]
  {:content (str "\"" unicode "\"")
   :font-family "FontAwesome"})

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

(defn side-button [icon side]
  (let [width (em 1.5)]
    [:&
     {:background "#cad2d3"
      :position "absolute"
      :top "4em"
      :width width
      :height "3em"
      :display "block"
      :line-height "3em"
      :text-decoration "none"
      :text-align "center"
      :color "white"}

     (case side
       :left {:left (m/- width)}
       :right {:right (m/- width)})

     [:&:before
      (fontawesome icon)]

     [:&:hover
      {:background accent-color}]]))

; UNUSED
(defn entity-box-styles []
  (let [size "85px"]
    [:&
     {:display "inline-block"
      :border "1px solid #ededed"
      :width size
      :padding "0.5em"
      :margin [[0 "0.5em" "0.5em" 0]]
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
      {:border-color "#aaa"}]]))


(def entity-list-styles
  (let [pad (rem 0.75)
        height (rem 2.6)]
    [:.entity-list
     {:overflow-x "hidden"
      :overflow-y "scroll"}

     [:.entity
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
       {:color "#AAA"}]]

     ]))

(def entity-styles
  [:>.active-entity

   [:>a.edit
    (side-button \uf040 :right)]

   [:>.entity

    (let [height "50vh"]
      [:>.photo-mosaic

       [:>.many
        {:display "flex"
         :overflow "hidden"
         :max-height height}

        [:>.col
         {:display "flex"
          :flex-direction "column"
          :width "50%"}

         [:>a
          {:display "block"}

          [:>img
           {:display "block"
            :width "100%"}]]]]

       [:>.single

        [:>a
         {:display "block"}

         [:>img
          {:display "block"
           :width "100%"}]]]])

    [:>.photo

     [:>img
      {:display "block"
       :max-width "100%"}]]]

   [:.info
    {:padding "1.5em"
     :background accent-color
     :color "white"}

    [:a
     {:color "white"
      :text-decoration "none"}

     [:&:hover
      {:text-decoration "underline"}]]

    [:h1
     {:font-size "1.25em"}]

    [:h2
     {:font-size "1em"
      :font-weight "normal"
      :display "flex"
      :justify-content "space-between"}]]

   [:.meta
    {:padding "1.5em"}

    [:.row
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

   [:.related
    [:h2
     {:font-size "1em"
      :margin-left (rem 0.75)
      :padding [[(rem 0.75) 0]]}]]])

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
   {:overflow-y "scroll"}

   [:>.no-results
    {:padding [[pad]]}]

   [:>.group

    [:>h2
     {:font-size "1em"
      :margin-left pad
      :padding [[pad 0]]}]]])

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
     :left "2em"
     :top 0
     :min-width "19em"
     :width "30%"
     :max-height "100vh"
     :z-index 1000
     :display "flex"
     :flex-direction "column"}
    (floating-box)

    [:>a.back
     (side-button \uf0d9 :left)]

    search-query-styles

    [:>.content
     {:overflow-y "scroll"}

     search-result-styles
     entity-styles
     entity-list-styles]]

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


