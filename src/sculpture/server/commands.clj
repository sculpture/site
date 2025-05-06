(ns sculpture.server.commands
  (:require
    [clojure.string :as string]
    [tada.events.core :as tada]
    [sculpture.db.pg.select :as db.select]
    [sculpture.server.advanced-search :as advanced-search]))

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
      (db.select/search {:query query
                         :types types
                         :limit (Integer. limit)}))}

   {:id :advanced-search
    :params {;; TODO one of the allowed types
             :entity-type NonBlankString?
             ;; TODO spec this out
             :conditions any?}
    :rest [:post "/api/graph/advanced-search"]
    :return
    (fn [{:keys [entity-type conditions]}]
      (advanced-search/search
        entity-type
        conditions))}])

(def commands
  [])

(tada/register! (concat queries commands))

