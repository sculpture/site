(ns sculpture.darkroom.core
  (:require
    [clojure.string :as string]
    [sculpture.darkroom.convert :as convert]
    [sculpture.darkroom.s3 :as s3]
    [sculpture.server.db :as db]))

(defn upload-image! [id file]
  (let [file-name (str id ".jpg")]
    (doseq [[size quality folder] [[40 5 "preload/"]
                                   [100 75 "thumb/"]
                                   [512 75 "medium/"]
                                   [1024 95 "large/"]
                                   [99999 100 "original/"]]]
      (let [path (str folder file-name)]
        (println folder)
        (if (s3/object-exists? path)
          (println "Exists. Skipping.")
          (-> (convert/convert! file size quality)
              (s3/upload! path)))))))

(defn extract-data [file]
  {:colors (convert/extract-colors file)
   :created-at (convert/extract-created-at file)
   :dimensions (convert/extract-dimensions file)})

(defn process-image! [id file user-id]
  {:pre [(uuid? id)
         (uuid? user-id)]}
  (upload-image! id file)
  (let [image-data (extract-data file)]
    (db/upsert! {:id id
                 :type "photo"
                 :user-id user-id
                 :colors (vec (image-data :colors))
                 :captured-at (image-data :created-at)
                 :width (get-in image-data [:dimensions :width])
                 :height (get-in image-data [:dimensions :height])}
                user-id))
  (db/select {:id id}))

(defn delete! [id]
  (doseq [folder ["preload/" "thumb/" "medium/" "large/" "original/"]]
    (s3/delete! (str folder id ".jpg"))))