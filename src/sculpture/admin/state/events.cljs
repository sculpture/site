(ns sculpture.admin.state.events
  (:require
    [re-frame.core :refer [reg-event-fx dispatch]]
    [sculpture.admin.state.search :as search]))

(defn key-by-id [arr]
  (reduce (fn [memo a]
            (assoc memo (a :id) a))
          {}
          arr))

(reg-event-fx
  :init
  (fn [{db :db} _]
    {:db {:query ""
          :active-entity-id nil
          :edit? false
          :results nil
          :page nil
          :data {}
          :fuse nil}
     :ajax {:method :get
            :uri "http://localhost:2468/all"
            :on-success
            (fn [data]
              (dispatch [:init-data data]))}}))

(reg-event-fx
  :init-data
  (fn [{db :db} [_ data]]
    {:db (assoc db
           :data (key-by-id data)
           :fuse (search/init data))}))

(reg-event-fx
  :set-query
  (fn [{db :db} [_ query]]
    {:db (assoc db :query query)
     :dispatch-debounce {:id :query
                         :timeout 250
                         :dispatch [:set-results query]}}))

(reg-event-fx
  :set-results
  (fn [{db :db} [_ query]]
    {:db (assoc db :results
           (map
             (fn [id]
               (get-in db [:data id]))
             (search/search (db :fuse) query 20)))}))

(reg-event-fx
  :set-page
  (fn [{db :db} [_ page]]
    {:db (assoc db :page page)}))

(reg-event-fx
  :update
  (fn [{db :db} [_ id k v]]
    {:db (assoc-in db [:data id k] v)}))
