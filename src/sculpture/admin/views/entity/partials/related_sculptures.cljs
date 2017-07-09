(ns sculpture.admin.views.entity.partials.related-sculptures
  (:require
    [sculpture.admin.views.entity.partials.list :refer [entity-list-view]]))

(defn related-sculptures-view [sculptures]
  [:div.sculptures
   [entity-list-view sculptures]])
