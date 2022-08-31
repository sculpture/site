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

   [:>.main
    {:width "100vw"
     :height "100vh"
     :display "flex"}

    [:>.sidebar
     {:min-width "19em"
      :width "40%"
      :height "100vh"
      :display "flex"
      :flex-direction "column"}

     [:>a.back
      (side-button \uf0d9 :left)]

     search-query-styles

     [:>.content
      {:overflow-y "auto"}

      search-result-styles
      entity-styles
      entity-list-styles]]

    [:>.mega-map
     {:height "100vh"
      :flex-grow 1
      :width "60%"}]]

   toolbar-styles

   [:>.page
    {:position "absolute"
     :top 0
     :left "30%"
     :margin-left "6em"
     :right "8em"
     :z-index 1000
     :max-height "100vh"
     :overflow-y "auto"
     :box-sizing "border-box"}
    (floating-box)

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
       :margin "1em"}]]

    [:>.page.actions]

    [:>.page.upload

     [:>.content

      [:>.uploading

       [:>.progress
        {:width "100px"}

        [:>.bar
         {:height "5px"
          :background colors/accent-color
          :transition "width 1s ease-in-out"}]]]]]

    [:>.page.advanced-search
     [:>.content
      [:>.conditions
       [:>.condition
        {:display "flex"}
        [:>.key]
        [:>.option]
        [:>.value]]]

      [:>.results
       {:width "100%"}
       entity-list-styles]]]

    editor-styles]])
