(ns sculpture.admin.state.core
  (:require
    [re-frame.core :as reframe :refer [reg-sub]]
    [sculpture.admin.state.util :refer [reg-event-fx]]
    ;; register events and subs:
    [sculpture.admin.state.advanced-search]
    [sculpture.admin.state.auth]
    [sculpture.admin.state.edit]
    [sculpture.admin.state.mega-map]
    [sculpture.admin.state.search]))

(defn key-by-id [arr]
  (reduce (fn [memo a]
            (assoc memo (a :id) a))
          {}
          arr))

(reg-event-fx
  :state.core/initialize!
  (fn [_ _]
    {:db {:db/search {:query ""
                      :results nil
                      :focused? false}
          :db/user nil
          :active-entity-id nil
          :db/entity-draft nil
          :saving? false
          :db/main-page nil
          :db/advanced-search nil
          :db/mega-map {:sculptures []
                        :dirty? false}}
     :dispatch-n [[:state.auth/check-auth!]
                  [:state.mega-map/remote-get-map-sculptures!]]}))
;; sculpture.data

(reg-event-fx
  :state.core/remote-eql!
  (fn [{} [_ identifier pattern callback]]
    {:ajax {:method :post
            :uri "/api/eql"
            :params {:identifier identifier
                     :pattern pattern}
            :on-success callback}}))

;; set-main-page

(reg-event-fx
  :state.core/set-main-page!
  (fn [{db :db} [_ page]]
    {:db (assoc db :db/main-page page)}))

(reg-sub
  :state.core/main-page
  (fn [db _]
    (db :db/main-page)))
