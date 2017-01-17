(ns sculpture.admin.state.events
  (:require
    [re-frame.core :refer [dispatch] :as reframe]
    [cljs-uuid-utils.core :as uuid]
    [sculpture.admin.state.fx.dispatch-debounce]
    [sculpture.admin.state.fx.ajax]
    [sculpture.admin.state.fx.redirect]
    [sculpture.admin.routes :as routes]
    [sculpture.admin.state.spec :refer [check-state!]]
    [sculpture.admin.state.search :as search]))

(defn key-by-id [arr]
  (reduce (fn [memo a]
            (assoc memo (a :id) a))
          {}
          arr))

(def validate-schema-interceptor
  (reframe/after
    (fn [db [event-id]]
      (when-let [error-msg (check-state! db)]
        (js/console.error
          (str
            "Event " event-id
            " caused the state to be invalid:\n\n")
          error-msg)))))

(defn reg-event-fx
  ([id handler-fn]
   (reg-event-fx id nil handler-fn))
  ([id interceptors handler-fn]
    (reframe/reg-event-fx
        id
        [validate-schema-interceptor
         interceptors]
        handler-fn)))

(reg-event-fx
  :init
  (fn [{db :db} _]
    {:db {:query ""
          :active-entity-id nil
          :results nil
          :page nil
          :data nil
          :fuse nil
          :mega-map {:dirty? false}}
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
    {:redirect-to (routes/root-path)
     :db (assoc db :results
           (map
             (fn [id]
               (get-in db [:data id]))
             (search/search (db :fuse) query 20)))}))

(reg-event-fx
  :set-page
  (fn [{db :db} [_ page]]
    {:db (assoc db :page page)}))

(reg-event-fx
  :update-entity
  (fn [{db :db} [_ id k v]]
    {:db (assoc-in db [:data id k] v)}))

(reg-event-fx
  :remove-entity-key
  (fn [{db :db} [_ id k]]
    {:db (update-in db [:data id] (fn [e] (dissoc e k)))}))

(reg-event-fx
  :create-entity
  (fn [{db :db} _]
    (let [id (str (uuid/make-random-uuid))]
      {:db (assoc-in db [:data id] {:id id})
       :redirect-to (routes/entity-edit-path {:id id})})))

(reg-event-fx
  :sculpture.mega-map/go-to
  (fn [{db :db} [_ location]]
    {:db (-> db
             (assoc-in [:mega-map :dirty?] false)
             (assoc-in [:mega-map :center] location)
             (assoc-in [:mega-map :zoom-level] 15))}))

(reg-event-fx
  :sculpture.mega-map/show
  (fn [{db :db} [_ marker]]
    {:db (-> db
             (assoc-in [:mega-map :dirty?] false)
             (assoc-in [:mega-map :current-marker] marker))}))

(reg-event-fx
  :sculpture.mega-map/mark-as-dirty
  (fn [{db :db} _]
    {:db (assoc-in db [:mega-map :dirty?] true)}))
