(ns sculpture.admin.views.entity.partials.photos)

(def photo-host "http://sculpture.s3-website-us-east-1.amazonaws.com/")

(defn image-url [photo size]
  (if photo
    (case (or size :thumb)
      :thumb (str photo-host "thumb/" (photo :url))
      :medium (str photo-host "large/" (photo :url))
      :large (str photo-host "large/" (photo :url))
      :original (str photo-host "original/" (photo :url)))
    "http://placehold.it/50x50"))

(defn photo-view [{:keys [photo size attribution? on-click]}]
  [:div.photo {:on-click (or on-click (fn []))}
   [:img {:src (image-url photo size)}]
   (when attribution?
     [:div "By ..."])])


