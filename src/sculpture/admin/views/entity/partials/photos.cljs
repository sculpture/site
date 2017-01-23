(ns sculpture.admin.views.entity.partials.photos
  (:require
    [reagent.core :as r]))

(def photo-host "http://sculpture.s3-website-us-east-1.amazonaws.com/")

(defn image-url [photo size]
  (if photo
    (case (or size :thumb)
      :preload (str photo-host "thumb/" (photo :url))
      :thumb (str photo-host "thumb/" (photo :url))
      :medium (str photo-host "large/" (photo :url))
      :large (str photo-host "large/" (photo :url))
      :original (str photo-host "original/" (photo :url)))
    "http://placehold.it/50x50"))

(defn progressive-image-view [_]
  (let [canvas (r/atom nil)
        actual-loaded? (r/atom false)
        preview-loaded? (r/atom false)]
    (fn [{:keys [width height url-large url-preview color]}]
      [:div {:ref (fn [node]
                    (when (and node @canvas)
                      ; setting canvas dimensions here (vs as attrs below)
                      ; prevents a mysterious infinite loop
                      (set! (.. @canvas -width) (.-offsetWidth node))
                      (set! (.. @canvas -height) (.-offsetHeight node))))
             :style {:width "100%"
                     :padding-bottom (let [ratio (if (and width height)
                                                   (* 100 (/ height width))
                                                   100)]
                                       (str ratio "%"))
                     :position "relative"
                     :background-color color}}
       [:canvas
        {:ref (fn [node]
                (reset! canvas node))
         :style {:position "absolute"
                 :opacity (if @preview-loaded? 1 0)
                 :transition "opacity 0.25s ease-in-out"}}]
       [:img {:ref (fn [node]
                     ; setting the image src here (vs as an attr)
                     ; prevents the "tainted src" error when figwheel reloads
                     (when node
                       (set! (.-src node) url-preview)))
              :crossOrigin "Anonymous"
              :style {:display "none"}
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
                                                   20)
                           (reset! preview-loaded? true)))}]
       [:img {:src url-large
              :on-load (fn [_]
                         (reset! actual-loaded? true))
              :style {:opacity (if @actual-loaded? 1 0)
                      :position "absolute"
                      :transition "opacity 1s ease-in-out"
                      :width "100%"}}]])))

(defn image-view [{:keys [photo size] :as args}]
  (if (= size :thumb)
    [:img {:src (image-url photo size)}]
    [progressive-image-view {:url-large (image-url photo size)
                             :url-preview (image-url photo :preload)
                             :color (get-in photo [:colors 0])
                             :width (photo :width)
                             :height (photo :height)}]))

(defn photo-view [{:keys [photo size attribution? on-click]}]
  [:div.photo {:on-click (or on-click (fn []))}
   [image-view {:photo photo
                :size size}]

   (when attribution?
     [:div "By ..."])])


