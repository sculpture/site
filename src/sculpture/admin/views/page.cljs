(ns sculpture.admin.views.page
  (:require
    [sculpture.admin.state.core :refer [subscribe]]
    [sculpture.admin.views.pages.actions :refer [actions-view]]
    [sculpture.admin.views.pages.advanced-search :refer [advanced-search-view]]
    [sculpture.admin.views.pages.entity-editor :refer [entity-editor-view]]
    [sculpture.admin.views.pages.upload :refer [upload-view]]
    [sculpture.admin.views.pages.regions :refer [regions-view]]))

(defn page-view []
  (case @(subscribe [:main-page])
    :regions
    [:div.main
     [regions-view]]

    :edit
    [:div.main
     [entity-editor-view @(subscribe [:entity-draft])]]

    :actions
    [:div.main
     [actions-view]]

    :upload
    [:div.main
     [upload-view]]

    :advanced-search
    [:div.main
     [advanced-search-view]]

    nil))
