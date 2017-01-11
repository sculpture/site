(ns sculpture.admin.views.entity.partials.photos)

(def photo-host "http://sculpture.s3-website-us-east-1.amazonaws.com/")

(defn image-url [photo size]
  (if photo
    (case size
      :thumb (str photo-host "thumb/" (photo :url))
      :large (str photo-host "large/" (photo :url))
      :original (str photo-host "original/" (photo :url)))
    "http://placehold.it/50x50"))

(defn photo-view [photo size attribution?]
  [:div.photo
   [:img {:src (image-url photo size)}]
   (when attribution?
     [:div "By ..."])])


