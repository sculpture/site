(ns sculpture.admin.state.subs
  (:require
    [re-frame.core :refer [reg-sub]]
    [sculpture.admin.state.search :as search]))

(reg-sub
  :query
  (fn [db _]
    (db :query)))

(reg-sub
  :results
  (fn [db _]
    (db :results)))

(reg-sub
  :page
  (fn [db _]
    (db :page)))

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
    (->> (search/search (db :fuse) query 20)
         (map (fn [id]
                (get-in db [:data id])))
         (filter (fn [entity]
                   (= (entity :type) type))))))

(reg-sub
  :sculptures-for-artist
  (fn [db [_ artist-id]]
    (->> db
         :data
         vals
         (filter (fn [entity]
                   (and
                     (= "sculpture" (entity :type))
                     (contains? (set (entity :artist-ids)) artist-id)))))))

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
