(ns sculpture.darkroom.convert
  (:require
    [clojure.string :as string]
    [clj-time.format :as tf]
    [clj-time.coerce :as tc]
    [me.raynes.fs :as fs]
    [me.raynes.conch :refer [with-programs]]))

(defonce temp-dir (fs/temp-dir "sculpture-photos"))

(defn convert!
  "returns a temp file (file shrunk to fit within maxsize, using imagemagick)"
  [file {:keys [maxsize quality sharpen]}]
  (let [in-path (.getPath file)
        out-path (str (.getPath temp-dir) "/" maxsize "-" (.getName file))
        temp-file-path (str out-path ".tmp.jpg")
        out-file (java.io.File. out-path)]
    (println "Converting..." (.getName file))
    (with-programs [convert cjpeg]
      (let [convert-args (->> [in-path
                               "-format" "JPG"
                               "-resize" (str maxsize "x" maxsize ">")
                               "-quality" 100
                               "-strip"
                               (when sharpen
                                 ["-adaptive-sharpen" "0x0.6"])
                               temp-file-path]
                              flatten
                              (remove nil?))]
        (apply convert convert-args))
      (cjpeg "-quality" quality "-baseline" temp-file-path
             {:binary true
              :out out-file})
      out-file)))

#_(convert! (clojure.java.io/file "./dev-resources/color-test.jpeg")
            {:maxsize 1000
             :quality 90
             :sharpen false})

(defn extract-colors
  "Given a file, returns list of top 3 colors in HEX"
  [file]
  (with-programs [convert]
    (-> (convert (.getPath file)
                 "-filter" "Spline"
                 "-scale" "50x50"
                 "-dither" "None"
                 "-colorspace" "LAB"
                 "-colors" 3
                 "-format" "%c" "histogram:info:-")
        ; returns list of colors in format:
        ;    687: ( 23, 45, 18) #172D12 srgb(23,45,18)
        (string/split #"\n")
        sort
        reverse
        (->> (map (fn [result]
                    (second (re-matches #".*(#[0-9A-F]{6}).*" result))))))))

#_(extract-colors (clojure.java.io/file "./dev-resources/color-test.jpeg"))

(defn extract-dimensions [file]
  (with-programs [identify]
    (let [[w h] (-> (identify "-ping" "-format" "%w %h" (.getPath file))
        (string/split #" "))]
      {:width (Integer. w)
       :height (Integer. h)})))

#_(extract-dimensions (clojure.java.io/file "./dev-resources/color-test.jpeg"))

(defn extract-created-at [file]
  (with-programs [identify]
    (or
      (try
        (->> (identify "-ping" "-format" "%[EXIF:DateTimeOriginal]" (.getPath file))
             (tf/parse (tf/formatter "YYYY:MM:dd HH:mm:ss"))
             tc/to-date)
        (catch java.lang.IllegalArgumentException e))
      (try
        (->> (identify "-ping" "-format" "%[EXIF:DateTime]" (.getPath file))
             (tf/parse (tf/formatter "YYYY:MM:dd HH:mm:ss"))
             tc/to-date)
        (catch java.lang.IllegalArgumentException e))
      (try
        (->> (identify "-ping" "-format" "%[date:create]" (.getPath file))
             (tf/parse (tf/formatter :date-time-no-ms))
             tc/to-date)
        (catch java.lang.IllegalArgumentException e))
      (java.util.Date. (.lastModified file)))))

#_(extract-created-at (clojure.java.io/file "./dev-resources/color-test.jpeg"))

(defn- raw-gps-to-float [coords ref]
  (let [numbers (->> (string/split coords #",")
                     (map (fn [equation]
                            (let [components (->> (string/split equation #"/")
                                                  (map (fn [string] (Float/parseFloat string))))]
                              (/ (first components)
                                 (second components))))))
        degrees (+ (first numbers)
                   (/ (second numbers) 60)
                   (/ (last numbers) 3600))]
    (if (or (= ref "N") (= ref "E"))
      degrees
      (* -1 degrees))))

(defn extract-location [file]
  (try
    (with-programs [identify]
      (let [location-exif-string "%[EXIF:GPSLatitude]|%[EXIF:GPSLatitudeRef]|%[EXIF:GPSLongitude]|%[EXIF:GPSLongitudeRef]"
            [latitude latitude-ref longitude longitude-ref]
            (-> (identify "-ping" "-format" location-exif-string (.getPath file))
                (string/split #"\|"))]
        {:latitude (raw-gps-to-float latitude latitude-ref)
         :longitude (raw-gps-to-float longitude longitude-ref)}))
    (catch java.lang.Exception e)))

#_(extract-location (clojure.java.io/file "./dev-resources/color-test.jpeg"))
