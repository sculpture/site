(ns sculpture.admin.views.sidebar.entity
  (:require
    [sculpture.admin.views.sidebar.object :refer [object-view]]
    [sculpture.schema.schema :as schema]))

(defmulti entity-handler identity)

(defmethod entity-handler :default
  [type id]
  {:identifier {(keyword (name type) "id") id}
   :pattern (->> (schema/by-id (name type))
                 :entity/spec
                 keys)
   :view (fn [entity]
           [:div.entity
            [:div.info
             [object-view entity]]])})


