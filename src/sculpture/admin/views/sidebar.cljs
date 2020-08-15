(ns sculpture.admin.views.sidebar
  (:require
    [sculpture.admin.state.core :refer [subscribe dispatch!]]
    [sculpture.admin.routes :as routes]
    [sculpture.admin.views.sidebar.search :refer [query-view results-view]]
    [sculpture.admin.views.sidebar.entity :refer [entity-view]]
    [sculpture.admin.views.sidebar.entity.sculpture]
    [sculpture.admin.views.sidebar.entity.region]
    [sculpture.admin.views.sidebar.entity.city]
    [sculpture.admin.views.sidebar.entity.artist]
    [sculpture.admin.views.sidebar.entity.photo]
    [sculpture.admin.views.sidebar.entity.sculpture-tag]
    [sculpture.admin.views.sidebar.entity.material]))

(defn active-entity-view [entity-id]
  (when-let [entity @(subscribe [:get-entity entity-id])]
    [:div.active-entity
     (when @(subscribe [:user])
       [:button.edit {:on-click (fn [_]
                                  (dispatch! [:sculpture.edit/edit-entity entity-id]))}])
     [entity-view entity]]))

(defn sidebar-view []
  (let [page @(subscribe [:page])
        entity-id (when
                    (= :entity (:type page))
                    (page :id))
        typing-query? @(subscribe [:sculpture.search/query-focused?])
        query @(subscribe [:sculpture.search/query])]
    [:div.sidebar
      [query-view]

      (when (not= :root (:type page))
        [:a.back.button {:href (routes/root-path)}])

      [:div.content
       (cond
         (and typing-query? (seq query))
         [results-view]

         entity-id
         [active-entity-view entity-id]

         (seq query)
         [results-view]

         :default
         [:div])]]))
