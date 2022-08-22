(ns sculpture.admin.state.subs
  (:require
    [malli.core :as m]
    [re-frame.core :refer [reg-sub]]
    [sculpture.schema.schema :as schema]))

(reg-sub
  :user
  (fn [db _]
    (db :user)))

(reg-sub
  :sculpture.search/query
  (fn [db _]
    (get-in db [:db/search :query])))

(reg-sub
  :sculpture.search/results
  (fn [db _]
    (get-in db [:db/search :results])))

(reg-sub
  :page
  (fn [db _]
    (db :page)))

(reg-sub
  :main-page
  (fn [db _]
    (db :main-page)))

(reg-sub
  :get-entity
  (fn [db [_ id]]
    (get-in db [:data id])))

(reg-sub
  :photos-for-sculpture
  (fn [db [_ sculpture-id]]
    (->> db
         :data
         vals
         (filter (fn [entity]
                   (and
                     (= "photo" (entity :type))
                     (= sculpture-id (entity :sculpture-id))))))))

(reg-sub
  :get-entities
  (fn [db [_ ids]]
    (map (fn [id]
           (get-in db [:data id])) ids)))

(reg-sub
  :entity-draft
  (fn [db _]
    (db :entity-draft)))

(reg-sub
  :sculpture.edit/invalid-fields
  (fn [db _]
    (->> (m/explain schema/Entity (db :entity-draft))
         :errors
         (mapcat :in)
         set)))

(reg-sub
  :sculpture.edit/saving?
  (fn [db _]
    (db :saving?)))

(reg-sub
  :sculpture.edit/entities-of-type
  (fn [db [_ type]]
    (->> db
         :data
         vals
         (filter (fn [entity]
                   (= type (entity :type)))))))



(reg-sub
  :sculptures-for
  (fn [db [_ filter-fn]]
    (->> db
         :data
         vals
         (filter (fn [entity]
                   (and
                     (= "sculpture" (entity :type))
                     (filter-fn entity)))))))

(reg-sub
  :sculpture-photos-for
  (fn [db [_ filter-fn]]
    (let [entities (->> db
                        :data
                        vals)
          sculpture-ids (->> entities
                             (filter (fn [entity]
                                       (and
                                         (= "sculpture" (entity :type))
                                         (filter-fn entity))))
                             (map :id)
                             set)
          photos (->> entities
                      (filter (fn [entity]
                                (and
                                  (= "photo" (entity :type))
                                  (contains? sculpture-ids (entity :sculpture-id))))))]
      photos)))

(reg-sub
  :sculpture.mega-map/sculptures
  (fn [db _]
    (db :db/map-sculptures)))

(reg-sub
  :sculpture.mega-map/config
  (fn [db _]
    (db :mega-map)))

(reg-sub
  :sculpture.search/query-focused?
  (fn [db _]
    (get-in db [:db/search :focused?])))

(reg-sub
  :sculpture.advanced-search/conditions
  (fn [db _]
    (get-in db [:advanced-search :conditions])))

(reg-sub
  :sculpture.advanced-search/results
  (fn [db _]
    (get-in db [:advanced-search :results])))

(reg-sub
  :sculpture.regions/all
  (fn [db _]
    (->> db
         :data
         vals
         (filter (fn [entity]
                   (= "region" (entity :type)))))))
