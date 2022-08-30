(ns sculpture.admin.views.page
  (:require
    [sculpture.admin.state.api :refer [subscribe]]
    [sculpture.admin.views.pages.actions :refer [actions-view]]
    [sculpture.admin.views.pages.advanced-search :refer [advanced-search-view]]
    [sculpture.admin.views.pages.entity-editor :refer [entity-editor-view]]
    [sculpture.admin.views.pages.upload :refer [upload-view]]
    [sculpture.admin.views.pages.regions :refer [regions-view]]))

(defn page-view []
  (case @(subscribe [:state.core/main-page])
    :main-page/regions
    [:div.main
     [regions-view]]

    :main-page/edit
    [:div.main
     [entity-editor-view @(subscribe [:state.edit/entity-draft])]]

    :main-page/actions
    [:div.main
     [actions-view]]

    :main-page/upload
    [:div.main
     [upload-view]]

    :main-page/advanced-search
    [:div.main
     [advanced-search-view]]

    nil))
