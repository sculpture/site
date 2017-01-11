(ns sculpture.admin.views.app
  (:require
    [re-frame.core :refer [subscribe dispatch]]
    [sculpture.admin.routes :as routes]
    [sculpture.admin.views.styles :refer [styles-view]]
    [sculpture.admin.views.search :refer [search-view]]
    [sculpture.admin.views.entity :refer [entity-view]]
    [sculpture.admin.views.entity.sculpture]
    [sculpture.admin.views.entity.artist]
    [sculpture.admin.views.entity-editor :refer [entity-editor-view]]))

(defn active-entity-view [entity-id edit?]
  (let [entity (subscribe [:get-entity entity-id])]
    (when @entity
      (if edit?
        [:div.entity.edit
         ^{:key (@entity :id)}
         [entity-editor-view @entity]]
        [:div.entity
         [:a {:href (routes/entity-edit-path {:id (@entity :id)})}
          "Edit"]
         [entity-view @entity]]))))

(defn app-view []
  (let [page @(subscribe [:page])]
    [:div.app
     [styles-view]
     [search-view]
     (when (and page (= (:type page) :entity))
       [active-entity-view (page :id) (page :edit?)])]))
