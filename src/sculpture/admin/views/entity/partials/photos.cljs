(ns sculpture.admin.views.entity.partials.photos
  (:require
    [reagent.core :as r]))

(def photo-host
   #_"https://s3-ca-central-1.amazonaws.com/sculpture-photos/"
   "https://1510904592.rsc.cdn77.org/")

(defn image-url [photo size]
  (if photo
    (case (or size :thumb)
      :preload (str photo-host "preload/" (photo :url))
      :thumb (str photo-host "thumb/" (photo :url))
      :medium (str photo-host "medium/" (photo :url))
      :large (str photo-host "large/" (photo :url))
      :original (str photo-host "original/" (photo :url)))
    "http://placehold.it/50x50"))

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

(defn image-view [{:keys [photo size] :as args}]
  (if (= size :thumb)
    [:div.image
     [:img {:src (image-url photo size)}]]
    ^{:key (photo :id)}
    [progressive-image-view {:url-large (image-url photo size)
                             :url-preview (image-url photo :preload)
                             :color (get-in photo [:colors 0])
                             :width (photo :width)
                             :height (photo :height)}]))

(defn photo-view [{:keys [photo size on-click]}]
  [:div.photo {:on-click (or on-click (fn []))}
   [image-view {:photo photo
                :size size}]])


