(ns sculpture.admin.views.sidebar.entity
  (:require
    [sculpture.admin.views.sidebar.object :refer [object-view]]))

(defmulti entity-view :type)

(defmethod entity-view :default
  [entity]
  [:div.entity
   [:div.info
    [object-view entity]]])


