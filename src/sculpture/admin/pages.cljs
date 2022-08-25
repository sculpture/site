(ns sculpture.admin.pages
  (:require
    [sculpture.admin.state.core :refer [dispatch!]]
    [sculpture.schema.schema :as schema]))

(defn view [])

(defn on-enter! []
  (dispatch! [:sculpture-set-query-focused! false]))

(def pages
  (concat
    [
     {:page/id :page/root
      :page/view #'view
      :page/path "/"
      :page/on-enter (fn [_]
                       (on-enter!))}

     {:page/id :page/entity
      :page/view #'view
      :page/path "/entity/:id"
      :page/parameters [:map
                        [:id uuid?]]
      :page/on-enter (fn [_]
                       (on-enter!))}]

  (for [entity-type schema/entity-types]
    {:page/id (keyword "page" entity-type)
     :page/view #'view
     :page/path (str "/" entity-type "/:id")
     :page/parameters [:map
                       [:id uuid?]]
     :page/on-enter (fn [_]
                      (on-enter!))})))

