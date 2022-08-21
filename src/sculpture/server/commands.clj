(ns sculpture.server.commands
  (:require
    [clojure.string :as string]
    [tada.events.core :as tada]
    [sculpture.db.pg.graph :as db.graph]))

;; for now, we have to use data-specs
(defn NonBlankString? [s]
  (and
    (string? s)
    (not (string/blank? s))))

(def queries
  [{:id :search
    :params {:query NonBlankString?
             :limit integer?}
    :rest [:get "/api/graph/search"]
    :return
    (fn [{:keys [query limit]}]
      (db.graph/search {:query query
                        :limit (Integer. limit)}))}])

(def commands
  [])

(tada/register! (concat queries commands))

