(ns sculpture.admin.pages
  (:require
    [sculpture.admin.state.api :refer [dispatch!]]
    [sculpture.schema.schema :as schema]))

(defn view [])

(defn on-enter! []
  (dispatch! [:state.search/set-query-focused! false]))

(defn entity-type->page-id [entity-type]
  (keyword "page" entity-type))

(def pages
  (concat
    [
     {:page/id :page/root
      :page/view #'view
      :page/path "/"
      :page/on-enter (fn [_]
                       (on-enter!))}
     ]

  (for [entity-type schema/entity-types]
    {:page/id (entity-type->page-id entity-type)
     :page/view #'view
     :page/path (str "/" entity-type "/:id")
     :page/parameters [:map
                       [:id uuid?]]
     :page/on-enter (fn [_]
                      (on-enter!))})))

