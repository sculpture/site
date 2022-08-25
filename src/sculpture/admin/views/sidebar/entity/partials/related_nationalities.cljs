(ns sculpture.admin.views.sidebar.entity.partials.related-nationalities
  (:require
    [bloom.commons.pages :as pages]
    [sculpture.admin.state.core :refer [subscribe]]))

(defn related-nationality-view [nationality-id]
  (let [nationality @(subscribe [:get-entity nationality-id])]
    [:a.nationality {:href (pages/path-for [:page/nationality {:id (nationality :id)}])}
     (nationality :demonym)]))

(defn related-nationalities-view [nationality-ids]
  [:div.nationalities
   (interpose ", "
              (for [nationality-id nationality-ids]
                ^{:key nationality-id}
                [related-nationality-view nationality-id]))])
