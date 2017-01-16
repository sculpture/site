(ns sculpture.admin.views.entity
  (:require
    [sculpture.admin.views.object :refer [object-view]]))

(defmulti entity-view :type)

(defmethod entity-view :default
  [entity]
  [:div
   [:div.info
    [object-view entity]]])


