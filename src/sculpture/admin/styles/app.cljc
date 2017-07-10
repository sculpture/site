(ns sculpture.admin.styles.app
  (:require
    [garden.arithmetic :as m]
    [garden.units :refer [px em rem]]
    [sculpture.admin.styles.colors :as colors]
    [sculpture.admin.styles.button :refer [button]]
    [sculpture.admin.styles.editor :refer [editor-styles]]
    [sculpture.admin.styles.entity :refer [entity-styles]]
    [sculpture.admin.styles.entity-list :refer [entity-list-styles]]
    [sculpture.admin.styles.side-button :refer [side-button]]
    [sculpture.admin.styles.search :refer [search-query-styles search-result-styles]]
    [sculpture.admin.styles.toolbar :refer [toolbar-styles]]))

(defn floating-box []
  {:background "#fff"
   :box-shadow "0 0 2px 0 #ccc"})

(def app-styles
  [:>.app
   [:>.mega-map
    {:width "100vw"
     :height "100vh"}]

   toolbar-styles

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
     :left "30%"
     :margin-left "6em"
     :right "8em"
     :z-index 1000
     :max-height "100vh"
     :overflow-y "scroll"
     :box-sizing "border-box"}
    (floating-box)

    [:>div
     [:>.header
      {:display "flex"
       :padding "0.75rem"
       :background colors/accent-color
       :color "white"
       :align-items "center"
       :height "2rem"}

      [:>h1
       {:font-size "1.2em"
        :flex-grow 2}]

      [:>button.close
       (button :secondary)]

      [:>button.save
       {:margin-left "1em"}
       (button :primary)]]

     [:>.content
      {:display "flex"
       :flex-direction "column"
       :align-items "center"
       :padding "1em"}

      [:>button
       (button :secondary)
       {:width "13em"
        :margin "1em"}]]]

    [:>.actions]

    [:>.upload

     [:>.content

      [:>.uploading

       [:>.progress
        {:width "100px"}

        [:>.bar
         {:height "5px"
          :background colors/accent-color
          :transition "width 1s ease-in-out"}]]]]]

    editor-styles]])
