(ns sculpture.admin.views.entity.partials.related-tags
(:require
  [re-frame.core :refer [subscribe dispatch]]))

(defn related-tag-view [tag-id]
  (let [tag @(subscribe [:get-entity tag-id])]
    [:a.tag {:href ""
             :on-click (fn [e]
                         (.preventDefault e)
                         (dispatch [:set-active-entity-id (tag :id)]))}
     (tag :name)]))

(defn related-tags-view [tag-ids]
  [:div.tags
   (for [tag-id tag-ids]
     ^{:key tag-id}
     [related-tag-view tag-id])])
