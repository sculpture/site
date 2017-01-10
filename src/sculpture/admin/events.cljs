(ns sculpture.admin.events
  (:require
    [re-frame.core :refer [reg-event-fx dispatch]]))

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
          :data {}}
     :ajax {:method :get
            :uri "http://localhost:2468/all"
            :on-success
            (fn [data]
              (dispatch [:init-data data]))}}))

(reg-event-fx
  :init-data
  (fn [{db :db} [_ data]]
    {:db (assoc db :data (key-by-id data))}))

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
    (let [results (if (= 0 (count (db :query)))
                    []
                    (let [re-query (re-pattern (db :query))]
                      (->> db
                           :data
                           vals
                           (filter (fn [entity]
                                     (cond
                                       (entity :name)
                                       (re-find re-query (entity :name))

                                       (entity :title)
                                       (re-find re-query (entity :title))

                                       :else
                                       nil)))
                           (take 10))))]
      {:db (assoc db :results results)})))

(reg-event-fx
  :set-active-entity-id
  (fn [{db :db} [_ entity-id]]
    {:db (assoc db
           :active-entity-id entity-id
           :edit? false)}))

(reg-event-fx
  :edit
  (fn [{db :db} _]
    {:db (assoc db :edit? true)}))

(reg-event-fx
  :update
  (fn [{db :db} [_ id k v]]
    {:db (assoc-in db [:data id k] v)}))
