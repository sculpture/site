(ns sculpture.admin.state.events
  (:require
    [re-frame.core :refer [dispatch reg-fx] :as reframe]
    [cljs-uuid-utils.core :as uuid]
    [sculpture.admin.state.fx.dispatch-debounce :refer [dispatch-debounce-fx]]
    [sculpture.admin.state.fx.ajax :refer [ajax-fx]]
    [sculpture.admin.state.fx.redirect :refer [redirect-to-fx]]
    [sculpture.admin.state.fx.upload :refer [upload-fx]]
    [sculpture.admin.routes :as routes]
    [sculpture.admin.state.spec :refer [check-state! validate]]
    [sculpture.admin.state.search :as search]
    [sculpture.admin.state.advanced-search :as advanced-search]))

(reg-fx :ajax ajax-fx)
(reg-fx :upload upload-fx)
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
      (when-let [errors (check-state! db)]
        (js/console.error
          (str
            "Event " event-id
            " caused the state to be invalid:\n")
          (pr-str (map (fn [problem]
                         {:path (problem :path)
                          :pred (problem :pred)})
                       (:cljs.spec.alpha/problems errors))))))))

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
          :user nil
          :active-entity-id nil
          :entity-draft nil
          :saving? false
          :page nil
          :main-page nil
          :data nil
          :advanced-search {:conditions []}
          :mega-map {:dirty? false}}
     :dispatch-n [[:sculpture.user/-remote-auth]
                  [:sculpture.data/-remote-get-data]]}))


;; sculpture.user

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
    (dispatch [:sculpture.user/-remote-oauth token])))

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
  :sculpture.user/-remote-oauth
  (fn [_ [_ token]]
    {:ajax {:method :put
            :uri "/api/oauth/google/authenticate"
            :params {:token token}
            :on-success
            (fn [data]
              (dispatch [:sculpture.user/-handle-user-info data]))}}))

;; sculpture.data

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
    {:db (assoc db :data (key-by-id data))
     :dispatch [:sculpture.data/-reset-search-index]}))

(reg-event-fx
  :sculpture.data/-add-entity
  (fn [{db :db} [_ entity]]
    {:db (assoc-in db [:data (entity :id)] entity)
     :dispatch [:sculpture.data/-reset-search-index]}))

(reg-event-fx
  :sculpture.data/-reset-search-index
  (fn [{db :db} _]
    {:db (assoc-in db [:search :fuse] (search/init (vals (db :data))))}))

;; sculpture.search

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

;; set-page

(reg-event-fx
  :set-page
  (fn [{db :db} [_ page]]
    {:db (assoc db :page page)
     :dispatch [:sculpture.search/set-query-focused false]}))

;; set-main-page

(reg-event-fx
  :set-main-page
  (fn [{db :db} [_ page]]
    {:db (assoc db :main-page page)}))

; sculpture.photo

(reg-event-fx
  :sculpture.photo/upload
  (fn [{} [_ file {:keys [on-progress on-success on-error]}]]
    (let [id (uuid/make-random-uuid)]
      {:upload {:method :put
                :uri "/api/upload"
                :data {:id id
                       :file file}
                :on-progress on-progress
                :on-success on-success
                :on-error on-error}})))

;; sculpture.edit

(reg-event-fx
  :sculpture.edit/edit-entity
  (fn [{db :db} [_ id]]
    {:db (assoc db :entity-draft (get-in db [:data id]))
     :dispatch [:set-main-page :edit]}))

(reg-event-fx
  :sculpture.edit/stop-editing
  (fn [{db :db} _]
    {:db (assoc db :entity-draft nil)
     :dispatch [:set-main-page nil]}))

(reg-event-fx
  :sculpture.edit/save
  (fn [{db :db} _]
    (let [entity (db :entity-draft)]
      (if (nil? (validate entity))
        {:dispatch-n [[:sculpture.data/-add-entity entity]
                      [:sculpture.edit/-remote-persist-entity (entity :id)]]}
        {}))))

(reg-event-fx
  :sculpture.edit/-set-saving
  (fn [{db :db} [_ saving?]]
    {:db (assoc db :saving? saving?)}))

(reg-event-fx
  :sculpture.edit/-remote-persist-entity
  (fn [{db :db} [_ id]]
    {:dispatch [:sculpture.edit/-set-saving true]
     :ajax {:method :put
            :uri "/api/entities"
            :params {:entity (get-in db [:data id])}
            :on-success
            (fn [data]
              (dispatch [:sculpture.edit/-set-saving false]))
            :on-error
            (fn [_]
              (dispatch [:sculpture.edit/-set-saving false])
              (js/alert "There was an error saving."))}}))

(reg-event-fx
  :sculpture.edit/update-draft
  (fn [{db :db} [_ k v]]
    {:db (assoc-in db [:entity-draft k] v)}))

(reg-event-fx
  :sculpture.edit/remove-draft-key
  (fn [{db :db} [_ k]]
    {:db (update-in db [:entity-draft] (fn [e] (dissoc e k)))}))

(reg-event-fx
  :sculpture.edit/create-entity
  (fn [{db :db} [_ entity]]
    {:db (assoc db :entity-draft (merge {:id (uuid/make-random-uuid)}
                                         entity))
     :dispatch [:set-main-page :edit]}))

;; sculpture.mega-map

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

(reg-event-fx
  :sculpture.advanced-search/add-condition
  (fn [{db :db} _]
    {:db (update-in db [:advanced-search :conditions]
                    conj {:key nil
                          :option nil
                          :value nil})}))
(defn vec-remove
  "remove elem in coll"
  [coll pos]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))

(reg-event-fx
  :sculpture.advanced-search/remove-condition
  (fn [{db :db} [_ index]]
    {:db (update-in db [:advanced-search :conditions]
                    vec-remove index)}))

(reg-event-fx
  :sculpture.advanced-search/update-condition
  (fn [{db :db} [_ index k v]]
    {:db (assoc-in db [:advanced-search :conditions index k] v)}))

(reg-event-fx
  :sculpture.advanced-search/search
  (fn [{db :db} [_ index k v]]
    {:db (assoc-in db [:advanced-search :results] (advanced-search/get-results db))}))


