(ns sculpture.admin.views.entity.partials.photos
  (:require
    [sculpture.admin.views.entity.partials.progressive-image :refer [progressive-image-view]]))

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


