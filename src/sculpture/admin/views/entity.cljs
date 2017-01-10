(ns sculpture.admin.views.entity
  (:require
    [sculpture.admin.views.object :refer [object-view]]
    [re-frame.core :refer [subscribe dispatch]]))

(defmulti entity-view :type)

(defmethod entity-view :default
  [entity]
  [object-view entity])


