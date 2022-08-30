(ns sculpture.admin.state.advanced-search
  (:require
    [re-frame.core :refer [dispatch reg-sub]]
    [sculpture.admin.state.util :refer [reg-event-fx]]))

(defn vec-remove
  "remove elem in coll"
  [coll pos]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))

(reg-event-fx
  :state.advanced-search/clear!
  (fn [{db :db} _]
    {:db (assoc-in db [:db/advanced-search] nil)}))

(reg-event-fx
  :state.advanced-search/set-entity-type!
  (fn [{db :db} [_ entity-type]]
    {:db (-> db
             (assoc-in [:db/advanced-search :db.advanced-search/entity-type] entity-type)
             (assoc-in [:db/advanced-search :db.advanced-search/conditions] []))}))

(reg-event-fx
  :state.advanced-search/add-condition!
  (fn [{db :db} _]
    {:db (update-in db [:db/advanced-search :db.advanced-search/conditions]
                    (fnil conj [])
                    {:key nil
                     :option nil
                     :value nil})}))

(reg-event-fx
  :state.advanced-search/remove-condition!
  (fn [{db :db} [_ index]]
    {:db (update-in db [:db/advanced-search :db.advanced-search/conditions]
                    vec-remove index)}))

(reg-event-fx
  :state.advanced-search/update-condition!
  (fn [{db :db} [_ index k v]]
    {:db (assoc-in db [:db/advanced-search :db.advanced-search/conditions index k] v)}))

(reg-event-fx
  :state.advanced-search/search!
  (fn [{db :db} _]
    {:tada [:advanced-search
            {:entity-type (get-in db [:db/advanced-search :db.advanced-search/entity-type])
             :conditions (get-in db [:db/advanced-search :db.advanced-search/conditions])}
            {:on-success
             (fn [data]
               (dispatch [::store-results! data]))}]}))

(reg-event-fx
  ::store-results!
  (fn [{db :db} [_ data]]
    {:db (assoc-in db [:db/advanced-search :db.advanced-search/results] data)}))

(reg-event-fx
  :state.advanced-search/go!
  (fn [{db :db} [_ entity-type conditions]]
    {:db (-> db
             (assoc-in [:db/advanced-search :db.advanced-search/conditions] conditions)
             (assoc-in [:db/advanced-search :db.advanced-search/entity-type] entity-type))
     :dispatch-n [[:state.advanced-search/search!]
                  [:state.core/set-main-page! :main-page/advanced-search]]}))

;; SUBS

(reg-sub
  :state.advanced-search/entity-type
  (fn [db _]
    (get-in db [:db/advanced-search :db.advanced-search/entity-type])))

(reg-sub
  :state.advanced-search/conditions
  (fn [db _]
    (get-in db [:db/advanced-search :db.advanced-search/conditions])))

(reg-sub
  :state.advanced-search/results
  (fn [db _]
    (get-in db [:db/advanced-search :db.advanced-search/results])))
