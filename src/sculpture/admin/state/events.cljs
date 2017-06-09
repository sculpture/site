(ns sculpture.admin.state.events
  (:require
    [re-frame.core :refer [dispatch reg-fx] :as reframe]
    [cljs-uuid-utils.core :as uuid]
    [sculpture.admin.state.fx.dispatch-debounce :refer [dispatch-debounce-fx]]
    [sculpture.admin.state.fx.ajax :refer [ajax-fx]]
    [sculpture.admin.state.fx.redirect :refer [redirect-to-fx]]
    [sculpture.admin.routes :as routes]
    [sculpture.admin.state.spec :refer [check-state!]]
    [sculpture.admin.state.search :as search]))

(reg-fx :ajax ajax-fx)
(reg-fx :redirect-to redirect-to-fx)
(reg-fx :dispatch-debounce dispatch-debounce-fx)

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
  (fn [_ _]
    {:db {:search {:query ""
                   :results nil
                   :focused? false
                   :fuse nil}
          :user {:token nil
                 :email nil
                 :name nil
                 :avatar nil}
          :active-entity-id nil
          :page nil
          :data nil
          :mega-map {:dirty? false}}
     :dispatch-n [[:sculpture.user/-remote-auth]
                  [:sculpture.data/-remote-get-data]]}))


(reg-event-fx
  :sculpture.user/-remote-auth
  (fn [_ _]
    {:ajax {:method :get
            :uri "/api/session"
            :on-success
            (fn [data]
              (dispatch [:sculpture.user/-handle-user-info data]))}}))

(reg-event-fx
  :sculpture.user/-handle-user-info
  (fn [{db :db} [_ user]]
    {:db (assoc db :user user)}))

(defn message-event-handler [e]
  (let [token (.-data e)]
    (dispatch [:oauth/-remote-auth token])))

(defn attach-message-listener! []
  (js/window.addEventListener "message" message-event-handler))

(reg-event-fx
  :sculpture.user/authenticate
  (fn [_ _]
    (attach-message-listener!)
    (js/window.open "/api/oauth/google/request-token"
      "Log In to Sculpture"
      "width=500,height=700")
    {}))

(reg-event-fx
  :sculpture.user/-remote-auth
  (fn [_ [_ token]]
    {:ajax {:method :put
            :uri "/api/oauth/google/authenticate"
            :params {:token token}
            :on-success
            (fn [data]
              (dispatch [:sculpture.user/-handle-user-info data]))}}))

(reg-event-fx
  :sculpture.data/-remote-get-data
  (fn [_ _]
   {:ajax {:method :get
           :uri "/api/entities"
           :on-success
           (fn [data]
             (dispatch [:sculpture.data/-set-data data]))}}))

(reg-event-fx
  :sculpture.data/-set-data
  (fn [{db :db} [_ data]]
    {:db (-> db
             (assoc :data (key-by-id data))
             (assoc-in [:search :fuse]
               (search/init data)))}))

(reg-event-fx
  :sculpture.search/set-query-focused
  (fn [{db :db} [_ bool]]
    {:db (assoc-in db [:search :focused?] bool)}))

(reg-event-fx
  :sculpture.search/set-query
  (fn [{db :db} [_ query]]
    {:db (assoc-in db [:search :query] query)
     :dispatch-debounce {:id :query
                         :timeout 250
                         :dispatch [:sculpture.search/set-results query]}}))

(reg-event-fx
  :sculpture.search/set-results
  (fn [{db :db} [_ query]]
    {:db (assoc-in db [:search :results]
           (map
             (fn [id]
               (get-in db [:data id]))
             (search/search (get-in db [:search :fuse]) query 20)))}))

(reg-event-fx
  :set-page
  (fn [{db :db} [_ page]]
    {:db (assoc db :page page)
     :dispatch [:sculpture.search/set-query-focused false]}))

(reg-event-fx
  :sculpture.edit/update-entity
  (fn [{db :db} [_ id k v]]
    {:db (assoc-in db [:data id k] v)}))

(reg-event-fx
  :sculpture.edit/remove-entity-key
  (fn [{db :db} [_ id k]]
    {:db (update-in db [:data id] (fn [e] (dissoc e k)))}))

(reg-event-fx
  :sculpture.edit/create-entity
  (fn [{db :db} _]
    (let [id (str (uuid/make-random-uuid))]
      {:db (assoc-in db [:data id] {:id id
                                    :type "sculpture"})
       :redirect-to (routes/entity-edit-path {:id id})})))

(reg-event-fx
  :sculpture.mega-map/go-to
  (fn [{db :db} [_ location]]
    {:db (-> db
             (assoc-in [:mega-map :dirty?] false)
             (assoc-in [:mega-map :center] location)
             (assoc-in [:mega-map :zoom-level] 18))}))

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
