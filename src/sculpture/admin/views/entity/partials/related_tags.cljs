(ns sculpture.admin.views.entity.partials.related-tags
  (:require
    [sculpture.admin.state.core :refer [subscribe]]
    [sculpture.admin.routes :as routes]))

(defn related-tag-view [tag-id]
  (let [tag @(subscribe [:get-entity tag-id])]
    [:a.tag {:href (routes/entity-path {:id (tag :id)})}
     (tag :name)]))

(defn related-tags-view [tag-ids]
  [:div.tags
   (interpose ", "
              (for [tag-id tag-ids]
                ^{:key tag-id}
                [related-tag-view tag-id]))])
