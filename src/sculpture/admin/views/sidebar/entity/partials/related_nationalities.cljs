(ns sculpture.admin.views.sidebar.entity.partials.related-nationalities
  (:require
    [sculpture.admin.state.core :refer [subscribe]]
    [sculpture.admin.routes :as routes]))

(defn related-nationality-view [nationality-id]
  (let [nationality @(subscribe [:get-entity nationality-id])]
    [:a.nationality {:href (routes/entity-path {:id (nationality :id)})}
     (nationality :demonym)]))

(defn related-nationalities-view [nationality-ids]
  [:div.nationalities
   (interpose ", "
              (for [nationality-id nationality-ids]
                ^{:key nationality-id}
                [related-nationality-view nationality-id]))])
