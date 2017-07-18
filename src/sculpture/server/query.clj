(ns sculpture.server.query
  (:require
    [sculpture.server.db :as db]))

(defn entities-all []
  (db/all))
