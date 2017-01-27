(ns sculpture.admin.views.app
  (:require
    [sculpture.admin.state.core :refer [subscribe dispatch!]]
    [sculpture.admin.routes :as routes]
    [sculpture.admin.views.styles :refer [styles-view]]
    [sculpture.admin.views.search :refer [query-view results-view]]
    [sculpture.admin.views.mega-map :refer [mega-map-view]]
    [sculpture.admin.views.entity :refer [entity-view]]
    [sculpture.admin.views.entity.sculpture]
    [sculpture.admin.views.entity.region]
    [sculpture.admin.views.entity.artist]
    [sculpture.admin.views.entity.photo]
    [sculpture.admin.views.entity-editor :refer [entity-editor-view]]))

(defn edit-entity-view [entity-id]
  (let [entity (subscribe [:get-entity entity-id])]
    [:div.entity.edit
     [:a.button.view {:href (routes/entity-path {:id entity-id})} "X"]
     [entity-editor-view @entity]]))

(defn active-entity-view [entity-id]
  (let [entity (subscribe [:get-entity entity-id])]
    (when @entity
      [:div.active-entity
       [:a.edit.button {:href (routes/entity-edit-path {:id (@entity :id)})}]
       [entity-view @entity]])))

(defn new-entity-button-view []
  [:button.new {:on-click (fn [_]
                            (dispatch! [:sculpture.edit/create-entity]))}])

(defn toolbar-view []
  [:div.toolbar

   [new-entity-button-view]

   [:div.user
    [:img.avatar {:src "https://placehold.it/50x50/00ff00"}]]])

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

(defn app-view []
  (let [page @(subscribe [:page])]
    [:div.app
     [styles-view]
     [mega-map-view]
     [toolbar-view]
     [sidebar-view]

     (when (and page
             (= :entity (:type page))
             (:edit? page))
       [:div.main
        [edit-entity-view (page :id)]])]))
