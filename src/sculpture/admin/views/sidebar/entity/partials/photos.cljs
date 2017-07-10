(ns sculpture.admin.views.sidebar.entity.partials.photos
  (:require
    [sculpture.admin.cdn :as cdn]
    [sculpture.admin.views.sidebar.entity.partials.progressive-image :refer [progressive-image-view]]))

(defn image-view [{:keys [photo size] :as args}]
  (if (= size :thumb)
    [:div.image
     [:img {:src (cdn/image-url photo size)}]]
    ^{:key (photo :id)}
    [progressive-image-view {:url-large (cdn/image-url photo size)
                             :url-preview (cdn/image-url photo :preload)
                             :color (get-in photo [:colors 0])
                             :width (photo :width)
                             :height (photo :height)}]))

(defn photo-view [{:keys [photo size on-click]}]
  [:div.photo {:on-click (or on-click (fn []))}
   [image-view {:photo photo
                :size size}]])


