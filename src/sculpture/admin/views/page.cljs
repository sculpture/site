(ns sculpture.admin.views.page
  (:require
    [sculpture.admin.state.core :refer [subscribe]]
    [sculpture.admin.views.pages.actions :refer [actions-view]]
    [sculpture.admin.views.pages.entity-editor :refer [entity-editor-view]]
    [sculpture.admin.views.pages.upload :refer [upload-view]]))

(defn page-view []
  (case @(subscribe [:main-page])
    :edit
    [:div.main
     [entity-editor-view @(subscribe [:entity-draft])]]

    :actions
    [:div.main
     [actions-view]]

    :upload
    [:div.main
     [upload-view]]

    nil))
