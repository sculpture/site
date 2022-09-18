(ns sculpture.admin.state.edit
  (:require
    [cljs-uuid-utils.core :as uuid]
    [malli.core :as m]
    [re-frame.core :refer [dispatch reg-sub]]
    [sculpture.admin.state.util :refer [reg-event-fx]]
    [sculpture.schema.schema :as schema]
    [sculpture.schema.util :as schema.util]))

(reg-event-fx
  :state.edit/view-entity!
  (fn [_ [_ entity-type id]]
    {:dispatch-n [[:state.core/remote-eql!
                   {(keyword entity-type "id") id}
                   (schema.util/entity-keys entity-type)
                   (fn [data]
                     (dispatch [::store-draft! data]))]
                  [:state.core/set-main-page! :main-page/edit]]}))

(reg-event-fx
  ::store-draft!
  (fn [{db :db} [_ draft]]
    {:db (assoc db :db/entity-draft draft)}))

(reg-event-fx
  :state.edit/stop-editing!
  (fn [{db :db} _]
    {:db (assoc db :db/entity-draft nil)
     :dispatch [:state.core/set-main-page! nil]}))

(reg-event-fx
  :state.edit/save!
  (fn [{db :db} _]
    (let [entity (db :db/entity-draft)]
      (if (m/validate schema/Entity entity)
        {:dispatch-n [;; TODO retrigger currently showing data in sidebar
                      [::remote-persist-entity! entity]]}
        {}))))

(reg-event-fx
  ::set-saving!
  (fn [{db :db} [_ saving?]]
    {:db (assoc db :saving? saving?)}))

(reg-event-fx
  ::remote-persist-entity!
  (fn [{db :db} [_ entity]]
    {:dispatch [::set-saving! true]
     :ajax {:method :put
            :uri "/api/entities"
            :params {:entity entity}
            :on-success
            (fn [data]
              (dispatch [::set-saving! false]))
            :on-error
            (fn [_]
              (dispatch [::set-saving! false])
              (js/alert "There was an error saving."))}}))

(reg-event-fx
  :state.edit/update-draft!
  (fn [{db :db} [_ k v]]
    {:db (assoc-in db [:db/entity-draft k] v)}))

(reg-event-fx
  :state.edit/remove-draft-key!
  (fn [{db :db} [_ k]]
    {:db (update-in db [:db/entity-draft] (fn [e] (dissoc e k)))}))

(reg-event-fx
  :state.edit/create-entity!
  (fn [{db :db} [_ entity]]
    {:db (assoc db :db/entity-draft entity)
     :dispatch [:state.core/set-main-page! :main-page/edit]}))

(reg-event-fx
  :state.edit/geocode!
  (fn [_ [_ query callback]]
    {:ajax {:method :get
            :uri "/api/util/geocode"
            :params {:query query}
            :on-success callback
            :on-error
            (fn [_]
              (js/alert "Geocoding Error"))}}))

(reg-event-fx
  :state.edit/get-shape!
  (fn [_ [_ query callback]]
    {:ajax {:method :get
            :uri "/api/util/shape"
            :params {:query query}
            :on-success callback
            :on-error
            (fn [_]
              (js/alert "Fetching Shape Error"))}}))

(reg-event-fx
  :state.edit/simplify!
  (fn [_ [_ geojson callback]]
    {:ajax {:method :put
            :uri "/api/util/simplify"
            :params {:geojson geojson}
            :on-success callback
            :on-error
            (fn [_]
              (js/alert "Simplify Error"))}}))

(reg-event-fx
  :state.edit/upload-photo!
  (fn [{} [_ file {:keys [on-progress on-success on-error]}]]
    (let [id (uuid/make-random-uuid)]
      {:upload {:method :put
                :uri "/api/upload"
                :data {:id id
                       :file file}
                :on-progress on-progress
                :on-success on-success
                :on-error on-error}})))

;; SUBS

(reg-sub
  :state.edit/entity-draft
  (fn [db _]
    (db :db/entity-draft)))

(reg-sub
  :state.edit/saving?
  (fn [db _]
    (db :saving?)))

(reg-sub
  :state.edit/invalid-fields
  (fn [db _]
    (->> (m/explain schema/Entity (db :db/entity-draft))
         :errors
         (mapcat :in)
         set)))
