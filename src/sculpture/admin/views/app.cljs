(ns sculpture.admin.views.app
  (:require
    [sculpture.admin.state.core :refer [subscribe]]
    [sculpture.admin.routes :as routes]
    [sculpture.admin.views.styles :refer [styles-view]]
    [sculpture.admin.views.search :refer [search-view]]
    [sculpture.admin.views.map :refer [map-view]]
    [sculpture.admin.views.entity :refer [entity-view]]
    [sculpture.admin.views.entity.sculpture]
    [sculpture.admin.views.entity.region]
    [sculpture.admin.views.entity.artist]
    [sculpture.admin.views.entity-editor :refer [entity-editor-view]]))

(defn edit-entity-view [entity-id]
  (let [entity (subscribe [:get-entity entity-id])]
    [:div.entity.edit
     [:a.button.view {:href (routes/entity-path {:id entity-id})} "X"]
     [entity-editor-view @entity]]))

(defn active-entity-view [entity-id]
  (let [entity (subscribe [:get-entity entity-id])]
    (when @entity
      [:div.entity.view
       [:a.back.button {:href (routes/root-path)}
        "<< Search"]
       [:a.edit.button {:href (routes/entity-edit-path {:id (@entity :id)})}
        "Edit"]
       [entity-view @entity]])))

(defn app-view []
  (let [page @(subscribe [:page])]
    [:div.app
     [styles-view]
     [map-view]
     [:div.sidebar
      (case (:type page)
        :entity [active-entity-view (page :id) ]
        [search-view])]

      (when (and page
              (= :entity (:type page))
              (:edit? page))
        [:div.main
         [edit-entity-view (page :id)]])]))
