(ns sculpture.admin.views.photos
  (:require
    [re-frame.core :refer [subscribe]]))

(def photo-host "http://sculpture.s3-website-us-east-1.amazonaws.com/")

(defn image-url [photo size]
  (case size
    :thumb (str photo-host "thumb/" (photo :url))
    :large (str photo-host "large/" (photo :url))
    :original (str photo-host "original/" (photo :url))))

(defn photo-view [photo size attribution?]
  [:div.photo
   [:img {:src (image-url photo size)}]
   (when attribution?
     [:div "By ..."])])


