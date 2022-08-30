(ns sculpture.admin.views.sidebar.entity.partials.photos
  (:require
    [sculpture.admin.cdn :as cdn]
    [sculpture.admin.views.sidebar.entity.partials.progressive-image :refer [progressive-image-view]]))

(defn image-view [{:keys [photo size] :as args}]
  (if (= size :thumb)
    [:div.image
     [:img {:src (cdn/image-url photo size)}]]
    [progressive-image-view {:url-large (cdn/image-url photo size)
                             :url-preview (cdn/image-url photo :preload)
                             ;; colors were imported backwards?
                             :color (last (:photo/colors photo))
                             :width (:photo/width photo)
                             :height (:photo/height photo)}]))

(defn photo-view [{:keys [photo size on-click]}]
  [:div.photo {:on-click (or on-click (fn []))}
   [image-view {:photo photo
                :size size}]])


