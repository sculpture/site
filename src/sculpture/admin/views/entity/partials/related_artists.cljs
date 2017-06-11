(ns sculpture.admin.views.entity.partials.related-artists
  (:require
    [sculpture.admin.state.core :refer [subscribe]]
    [sculpture.admin.routes :as routes]))

(defn related-artist-view [artist-id]
  (let [artist @(subscribe [:get-entity artist-id])]
    [:a.artist {:href (routes/entity-path {:id (artist :id)})}
     (artist :name)]))

(defn related-artists-view [artist-ids]
  [:div.artists
   (for [artist-id artist-ids]
     ^{:key artist-id}
     [related-artist-view artist-id])])

