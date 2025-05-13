(ns sculpture.admin.state.mega-map
  (:require
    [re-frame.core :refer [dispatch reg-sub]]
    [sculpture.admin.state.util :refer [reg-event-fx]]))

(reg-event-fx
  :state.mega-map/remote-get-map-sculptures!
  (fn [_ _]
    {:ajax {:method :post
            :uri "/api/eql"
            :params {:identifier :sculptures
                     :pattern [:sculpture/id
                               :sculpture/location
                               :sculpture/title]}
            :on-success
            (fn [data]
              (dispatch [::set-map-sculptures! data]))}}))

(reg-event-fx
  ::set-map-sculptures!
  (fn [{db :db} [_ data]]
    {:db (assoc-in db [:db/mega-map :sculptures] data)}))

(reg-event-fx
  :state.mega-map/go-to!
  (fn [{db :db} [_ location]]
    {:db (-> db
             (assoc-in [:db/mega-map :dirty?] false)
             (assoc-in [:db/mega-map :center] location)
             (assoc-in [:db/mega-map :zoom-level] 18)
             (dissoc [:db/mega-map] :markers))}))

(reg-event-fx
  :state.mega-map/show!
  (fn [{db :db} [_ markers]]
    {:db (-> db
             (assoc-in [:db/mega-map :dirty?] false)
             (assoc-in [:db/mega-map :markers] markers))}))

(reg-event-fx
  :state.mega-map/mark-as-dirty!
  (fn [{db :db} _]
    {:db (assoc-in db [:db/mega-map :dirty?] true)}))

;; SUBS

(reg-sub
  :state.mega-map/config
  (fn [db _]
    (db :db/mega-map)))

