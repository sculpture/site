(ns sculpture.admin.views.entity.partials.related-sculptures
  (:require
    [sculpture.admin.state.core :refer [subscribe]]
    [sculpture.admin.routes :as routes]
    [sculpture.admin.views.entity.partials.photos :refer [photo-view]]
    [sculpture.admin.views.entity.partials.list :refer [entity-list-view]]))

(defn related-sculptures-view [sculptures]
  [:div.sculptures
   [entity-list-view sculptures]])
