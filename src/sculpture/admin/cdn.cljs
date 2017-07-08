(ns sculpture.admin.cdn)

(def photo-host
   #_"https://s3-ca-central-1.amazonaws.com/sculpture-photos/"
   "https://1510904592.rsc.cdn77.org/")

(defn image-url [photo size]
  (if photo
    (case (or size :thumb)
      :preload (str photo-host "preload/" (photo :id) ".jpg")
      :thumb (str photo-host "thumb/" (photo :id) ".jpg")
      :medium (str photo-host "medium/" (photo :id) ".jpg")
      :large (str photo-host "large/" (photo :id) ".jpg")
      :original (str photo-host "original/" (photo :id) ".jpg"))
    "http://placehold.it/50x50"))
