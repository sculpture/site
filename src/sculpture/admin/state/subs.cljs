(ns sculpture.admin.state.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sculpture.admin.state.spec :refer [validate]]
    [sculpture.admin.state.search :as search]))

(reg-sub
  :user
  (fn [db _]
    (db :user)))

(reg-sub
  :sculpture.search/query
  (fn [db _]
    (get-in db [:search :query])))

(reg-sub
  :sculpture.search/results
  (fn [db _]
    (get-in db [:search :results])))

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
    (let [validation-result (validate (db :entity-draft))
          invalid-fields (->> validation-result
                              (map :path)
                              (map last))
          missing-fields (->> validation-result
                              (map (fn [field]
                                     (last (re-find #".*cljs.core/contains\? % :([a-z\-]+)"
                                                    (str (field :pred))))))
                              (remove nil?)
                              (map keyword))]
      (set (concat invalid-fields missing-fields)))))

(reg-sub
  :sculpture.edit/saving?
  (fn [db _]
    (db :saving?)))

(reg-sub
  :related-entity-search
  (fn [db [_ type query]]
    (->> (search/search (get-in db [:search :fuse]) query 20)
         (map (fn [id]
                (get-in db [:data id])))
         (filter (fn [entity]
                   (= (entity :type) type))))))

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
  :sculptures
  (fn [db _]
    (->> db
         :data
         vals
         (filter (fn [entity]
                   (= "sculpture" (entity :type)))))))


(reg-sub
  :sculpture.mega-map/config
  (fn [db _]
    (db :mega-map)))

(reg-sub
  :sculpture.search/query-focused?
  (fn [db _]
    (get-in db [:search :focused?])))
