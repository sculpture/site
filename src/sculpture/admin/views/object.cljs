(ns sculpture.admin.views.object
  (:require
    [sculpture.admin.state.core :refer [subscribe]]
    [sculpture.admin.routes :as routes]))

(defmulti object-view
  (fn [entity]
    (or (when (map? entity) :map)
        (when (vector? entity) :vector)
        (when (and
                (string? entity)
                (re-matches #"http.*" entity))
          :link)
        (when (and
                (string? entity)
                (= 36 (count entity))
                (re-matches #"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}" entity))
          :id)
        (when (string? entity) :string))))

(defmethod object-view :default
  [entity]
  [:div (str entity)])

(defmethod object-view :map
  [entity]
  [:table
   [:tbody
    (for [[k v] entity]
      ^{:key k}
      [:tr
       [:td (str k)]
       [:td
        [object-view v]]])]])

(defmethod object-view :vector
  [entity]
  [:div
   (for [o entity]
     ^{:key (gensym)}
     [object-view o])])

(defmethod object-view :string
  [entity]
  [:div entity])

(defmethod object-view :link
  [entity]
  [:a {:href entity} entity])

(defmethod object-view :id
  [entity-id]
  (let [entity @(subscribe [:get-entity entity-id])]
    [:a {:href (routes/entity-path {:id (entity :id)})}
     (or (entity :name)
         (entity :title)
         (entity :id))]))
