(ns sculpture.admin.views.sidebar
  (:require
    [reagent.core :as r]
    [bloom.commons.pages :as pages]
    [bloom.commons.ajax :as ajax]
    [sculpture.admin.state.api :refer [subscribe dispatch!]]
    [sculpture.admin.views.sidebar.search :refer [query-view results-view]]
    [sculpture.admin.views.sidebar.entity :refer [entity-handler]]
    [sculpture.admin.views.sidebar.entity.sculpture]
    [sculpture.admin.views.sidebar.entity.region]
    [sculpture.admin.views.sidebar.entity.city]
    [sculpture.admin.views.sidebar.entity.artist]
    [sculpture.admin.views.sidebar.entity.photo]
    [sculpture.admin.views.sidebar.entity.sculpture-tag]
    [sculpture.admin.views.sidebar.entity.material]))

(defn entity-fetcher-view
  [entity-type entity-id]
  (r/with-let [entity (r/atom nil)
               c (entity-handler entity-type entity-id)
               _ (ajax/request {:method :post
                                :uri "/api/eql"
                                :params (select-keys c [:identifier :pattern])
                                :on-success
                                (fn [data]
                                  (reset! entity data))})]
    (when @entity
      [(:view c) @entity])))

(defn active-entity-view [page-id entity-id]
  [:div.active-entity
   (when @(subscribe [:state.auth/user])
     [:button.edit {:on-click (fn [_]
                                (dispatch! [:state.edit/view-entity!
                                            (name page-id)
                                            entity-id]))}])
   ^{:key entity-id}
   [entity-fetcher-view (keyword (name page-id)) entity-id]])

(defn sidebar-view []
  (let [[page-id page-params] (pages/->args @pages/current-page)
        entity-id (when
                    (not= :page/root page-id)
                    (:id page-params))
        typing-query? @(subscribe [:state.search/query-focused?])
        query @(subscribe [:state.search/query])]
    [:div.sidebar
      [query-view]

      [:div.content
       (cond
         (and typing-query? (seq query))
         [results-view]

         entity-id
         [active-entity-view page-id entity-id]

         (seq query)
         [results-view]

         :else
         [:div])]]))
