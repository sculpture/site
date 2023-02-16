(ns sculpture.admin.views.sidebar.entity.region-tag
  (:require
    [bloom.commons.pages :as pages]
    [sculpture.admin.state.api :refer [dispatch!]]
    [sculpture.admin.views.sidebar.entity :refer [entity-handler]]
    [sculpture.admin.views.sidebar.entity.partials.list :refer [entity-list-view]]
    [sculpture.admin.views.sidebar.entity.partials.related-sculptures :refer [related-sculptures-view]]
    [sculpture.admin.views.sidebar.entity.partials.map :refer [map-view]]))

(defn region-tag-entity-view
  [region-tag]
  [:div.region-tag.entity
   [:div.info
    [:h1 (:region-tag/name region-tag)]]
   [:div.related
    [:h2 "Regions"]
    [:div.regions
     [entity-list-view (:region-tag/regions region-tag)]]]])

(defmethod entity-handler :region-tag
  [_ region-tag-id]
  {:identifier {:region-tag/id region-tag-id}
   :pattern [:region-tag/id
             :region-tag/name
             {:region-tag/regions
              [:region/id
               :region/name]}]
   :view region-tag-entity-view})
