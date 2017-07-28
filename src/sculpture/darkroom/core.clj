(ns sculpture.darkroom.core
  (:require
    [clojure.string :as string]
    [sculpture.darkroom.convert :as convert]
    [sculpture.darkroom.s3 :as s3]
    [sculpture.db.core :as db]
    [sculpture.db.pg.select :as db.select]))

(defn upload-image! [id file]
  (let [file-name (str id ".jpg")]
    (doseq [[folder convert-opts] [["preload/" {:maxsize 40 :quality 5}]
                                   ["thumb/" {:maxsize 100 :quality 75 :sharpen true}]
                                   ["medium/" {:maxsize 512 :quality 75 :sharpen true}]
                                   ["large/" {:maxsize 1024 :quality 95}]
                                   ["original/" nil]]]
      (let [path (str folder file-name)]
        (println folder)
        (if (s3/object-exists? path)
          (println "Exists. Skipping.")
          (if convert-opts
            (-> (convert/convert! file convert-opts)
                (s3/upload! path))
            (s3/upload! file path)))))))

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
                 :height (get-in image-data [:dimensions :height])
                 :sculpture-id nil}
                user-id))
  (db.select/select-entity-with-id "photo" id))

(defn delete! [id]
  (doseq [folder ["preload/" "thumb/" "medium/" "large/" "original/"]]
    (s3/delete! (str folder id ".jpg"))))
