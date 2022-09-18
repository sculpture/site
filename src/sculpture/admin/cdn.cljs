(ns sculpture.admin.cdn)

(def photo-host
   #_"https://s3-ca-central-1.amazonaws.com/sculpture-photos/"
   "https://1510904592.rsc.cdn77.org/")

(defn image-url [{:photo/keys [id] :as photo} size]
  (if id
    (case (or size :thumb)
      :preload (str photo-host "preload/" id ".jpg")
      :thumb (str photo-host "thumb/" id ".jpg")
      :medium (str photo-host "medium/" id ".jpg")
      :large (str photo-host "large/" id ".jpg")
      :original (str photo-host "original/" id ".jpg"))
    "http://via.placeholder.com/50x50"))
