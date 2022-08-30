(ns sculpture.darkroom.core
  (:require
    [clojure.string :as string]
    [sculpture.darkroom.convert :as convert]
    [sculpture.darkroom.s3 :as s3]
    [sculpture.db.core :as db]
    [sculpture.db.pg.select :as db.select]))

(defn convert-and-upload-image! [id file]
  (let [file-name (str id ".jpg")]
    (doseq [[folder convert-opts] [["preload/" {:maxsize 40 :quality 5}]
                                   ["thumb/" {:maxsize 100 :quality 75 :sharpen true}]
                                   ["preview/" {:maxsize 300 :quality 75 :sharpen true}]
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
   :dimensions (convert/extract-dimensions file)
   :location (convert/extract-location file)})

(defn process-image! [id file]
  {:pre [(uuid? id)]}
  (convert-and-upload-image! id file)
  (extract-data file))

(defn delete! [id]
  (doseq [folder ["preload/" "thumb/" "medium/" "large/" "original/"]]
    (s3/delete! (str folder id ".jpg"))))
