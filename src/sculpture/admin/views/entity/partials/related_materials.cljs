(ns sculpture.admin.views.entity.partials.related-materials
(:require
  [re-frame.core :refer [subscribe dispatch]]))

(defn related-material-view [material-id]
  (let [material @(subscribe [:get-entity material-id])]
    [:a.material {:href ""
                  :on-click (fn [e]
                              (.preventDefault e)
                              (dispatch [:set-active-entity-id (material :id)]))}
     (material :name)]))

(defn related-materials-view [material-ids]
  [:div.materials
   (for [material-id material-ids]
     ^{:key material-id}
     [related-material-view material-id])])
