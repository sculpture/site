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
             :limit pos-int?
             ;; TODO one of the allowed types
             :types [NonBlankString?]}
    :rest [:get "/api/graph/search"]
    :return
    (fn [{:keys [query limit types]}]
      (let [types (set types)]
        (->> (db.graph/search {:query query
                               :limit (Integer. limit)})
             (filter (fn [{:keys [type]}]
                       (contains? types type))))))}])

(def commands
  [])

(tada/register! (concat queries commands))

