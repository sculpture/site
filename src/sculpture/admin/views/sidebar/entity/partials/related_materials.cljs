(ns sculpture.admin.views.sidebar.entity.partials.related-materials
  (:require
    [sculpture.admin.state.core :refer [subscribe]]
    [sculpture.admin.routes :as routes]))

(defn related-material-view [material-id]
  (let [material @(subscribe [:get-entity material-id])]
    [:a.material {:href (routes/entity-path {:id (material :id)})}
     (material :name)]))

(defn related-materials-view [material-ids]
  [:div.materials
   (interpose ", "
              (for [material-id material-ids]
                ^{:key material-id}
                [related-material-view material-id]))])
