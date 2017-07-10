(ns sculpture.admin.views.sidebar.entity.partials.progressive-image
  (:require
    [reagent.core :as r]))

(defn progressive-image-view [_]
  (let [canvas (r/atom nil)]
    (fn [{:keys [width height url-large url-preview color]}]
      [:div.image
       {:ref (fn [node]
               (when (and node @canvas)
                 ; setting canvas dimensions here (vs as attrs below)
                 ; prevents a mysterious infinite loop
                 (set! (.. @canvas -width) (.-offsetWidth node))
                 (set! (.. @canvas -height) (.-offsetHeight node))))
        :style {:overflow "hidden"
                :display "inline-block"
                :width "100%"
                :padding-bottom (let [ratio (if (and width height)
                                               (* 100 (/ height width))
                                               100)]
                                  (str ratio "%"))
                :position "relative"
                :background-color color}}
       ; if reagent redraws this component, the canvas is cleared
       ; instead of setting opacity using reagent, it's done statefully via js
       [:canvas
        {:ref (fn [node]
                (when (and node (not @canvas))
                  (reset! canvas node)))
         :style {:opacity 0
                 :position "absolute"
                 :transition "opacity 0.25s ease-in-out"}}]
       [:img {:crossOrigin "Anonymous"
              :style {:display "none"}
              :src url-preview
              :on-load (fn [e]
                         (let [img (.. e -target)]
                           (.drawImage (.getContext @canvas "2d") img
                                       0 0
                                       (.-offsetWidth @canvas)
                                       (.-offsetHeight @canvas))
                           (js/StackBlur.canvasRGB @canvas
                                                   0 0
                                                   (.-offsetWidth @canvas)
                                                   (.-offsetHeight @canvas)
                                                   30)
                           (set! (.. @canvas -style -opacity) 1)))}]
       [:img {:src url-large
              :on-load (fn [e]
                         (set! (.. e -target -style -opacity) 1))
              :style {:opacity 0
                      :position "absolute"
                      :transition "opacity 0.75s ease-in-out"
                      :width "100%"}}]])))
