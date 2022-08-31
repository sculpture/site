(ns sculpture.admin.views.sidebar.entity.partials.related
  (:require
    [bloom.commons.pages :as pages]))

(defn related-view
  [{:keys [entity-type label-key]} entities]
  (let [id-key (keyword (name entity-type) "id")]
    [:<>
     (interpose ", "
                (for [entity entities]
                  ^{:key (id-key entity)}
                  [:a {:class entity-type
                       :href (pages/path-for [(keyword "page" (name entity-type)) {:id (id-key entity)}])}
                   (label-key entity)]))]))


