(ns sculpture.admin.views.sidebar.entity.partials.related-materials
  (:require
    [bloom.commons.pages :as pages]
    [sculpture.admin.state.core :refer [subscribe]]))

(defn related-material-view [material-id]
  (let [material @(subscribe [:get-entity material-id])]
    [:a.material {:href (pages/path-for [:page/material {:id (material :id)}])}
     (material :name)]))

(defn related-materials-view [material-ids]
  [:div.materials
   (interpose ", "
              (for [material-id material-ids]
                ^{:key material-id}
                [related-material-view material-id]))])
