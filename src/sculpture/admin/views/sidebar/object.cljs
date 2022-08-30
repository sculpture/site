(ns sculpture.admin.views.sidebar.object)

(defmulti object-view
  (fn [entity]
    (or (when (map? entity) :map)
        (when (sequential? entity) :sequential)
        (when (and
                (string? entity)
                (re-matches #"http.*" entity))
          :link)
        (when (and
                (uuid? entity)
                #_(= 36 (count entity))
                #_(re-matches #"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}" entity))
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

(defmethod object-view :sequential
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
  [:div entity-id])
