(ns sculpture.admin.subs
  (:require
    [re-frame.core :refer [reg-sub]]))

(reg-sub
  :query
  (fn [db _]
    (db :query)))

(reg-sub
  :results
  (fn [db _]
    (db :results)))

(reg-sub
  :active-entity
  (fn [db _]
    (when (db :active-entity-id)
      (get-in db [:data (db :active-entity-id)]))))

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
  :edit?
  (fn [db _]
    (db :edit?)))

(reg-sub
  :related-entity-search
  (fn [db [_ type query]]
    (let [re-query (re-pattern query)]
      (if (= 0 (count query))
        []
        (->> db
             :data
             vals
             (filter (fn [entity]
                       (and
                         (= (entity :type) type)
                         (cond
                           (entity :name)
                           (re-find re-query (entity :name))

                           (entity :title)
                           (re-find re-query (entity :title))

                           :else
                           false)))))))))
