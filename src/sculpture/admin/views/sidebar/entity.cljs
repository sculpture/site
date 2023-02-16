(ns sculpture.admin.views.sidebar.entity
  (:require
    [sculpture.admin.views.sidebar.object :refer [object-view]]))

(defmulti entity-handler identity)

(defmethod entity-handler :default
  [type id]
  {:identifier {(keyword (name type) "id") id}
   :pattern (case type
              :user
              [:user/id
               :user/name]
              :nationality
              [:nationality/id
               :nationality/demonym]
              :category
              [:category/id
               :category/name])
   :view (fn [entity]
           [:div.entity
            [:div.info
             [object-view entity]]])})


