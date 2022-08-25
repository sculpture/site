(ns sculpture.admin.views.sidebar.entity.partials.related-tags
  (:require
    [bloom.commons.pages :as pages]
    [sculpture.admin.state.core :refer [subscribe]]))

(defn related-tag-view [tag-id]
  (let [tag @(subscribe [:get-entity tag-id])]
    [:a.tag {:href (pages/path-for [:page/entity {:id (tag :id)}])}
     (tag :name)]))

(defn related-tags-view [tag-ids]
  [:div.tags
   (interpose ", "
              (for [tag-id tag-ids]
                ^{:key tag-id}
                [related-tag-view tag-id]))])
