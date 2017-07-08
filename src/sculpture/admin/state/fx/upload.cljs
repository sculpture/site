(ns sculpture.admin.state.fx.upload
  (:require
    [clojure.string :as string]
    [cognitect.transit :as transit]
    [goog.events :as events])
  (:import
    (goog.net XhrIo EventType)))

(def reader
  (transit/reader :json {:handlers {"u" uuid}}))

(defn upload-fx
  [{:keys [uri method data on-success on-error on-progress timeout]}]
  (let [xhr (XhrIo.)
        body (js/FormData.)
        headers (clj->js {"Accept" "application/transit+json"})]

    (doseq [[k v] data]
      (.append body (name k) v))

    (when on-progress
      (.setProgressEventsEnabled xhr true)
      (events/listen xhr EventType.PROGRESS
        (fn [e] (on-progress {:loaded (.-loaded e)
                              :total (.-total e)}))))

    (when on-success
      (events/listen xhr EventType.SUCCESS
        (fn [_] (on-success (transit/read reader (.getResponseText xhr))))))

    (when on-error
      (events/listen xhr EventType.ERROR
        (fn [_] (on-error (.getResponseText xhr)))))

    (.send xhr uri (string/upper-case (name method)) body headers)))
