(ns sculpture.admin.state.search
  (:require
    [clojure.string :as string]
    [re-frame.core :refer [dispatch reg-sub]]
    [sculpture.admin.state.util :refer [reg-event-fx]]
    [sculpture.schema.schema :as schema]))

(reg-event-fx
  :state.search/set-query-focused!
  (fn [{db :db} [_ bool]]
    {:db (assoc-in db [:db/search :focused?] bool)}))

(reg-event-fx
  :state.search/set-query!
  (fn [{db :db} [_ query]]
    {:db (assoc-in db [:db/search :query] query)
     :dispatch-debounce {:id :query
                         :timeout 100
                         :dispatch [:state.search/remote-search! query
                                    schema/entity-types
                                    (fn [data]
                                      (dispatch [::set-results! data]))]}}))

(reg-event-fx
  :state.search/remote-search!
  (fn [{} [_ query types callback]]
    (when-not (string/blank? query)
      {:tada [:search
              {:query query
               :types types
               :limit 10}
              {:on-success callback}]})))

(reg-event-fx
  ::set-results!
  (fn [{db :db} [_ data]]
    {:db (assoc-in db [:db/search :results] data)}))

;; SUBS

(reg-sub
  :state.search/query-focused?
  (fn [db _]
    (get-in db [:db/search :focused?])))

(reg-sub
  :state.search/query
  (fn [db _]
    (get-in db [:db/search :query])))

(reg-sub
  :state.search/results
  (fn [db _]
    (get-in db [:db/search :results])))

