(ns sculpture.admin.views.app
  (:require
    [re-frame.core :refer [subscribe dispatch]]
    [sculpture.admin.routes :as routes]
    [sculpture.admin.views.search-result :refer [search-result-view]]
    [sculpture.admin.views.styles :refer [styles-view]]
    [sculpture.admin.views.entity :refer [entity-view]]
    [sculpture.admin.views.entity.sculpture]
    [sculpture.admin.views.entity.artist]
    [sculpture.admin.views.entity-editor :refer [entity-editor-view]]))

(defn active-entity-view [entity-id edit?]
  (let [entity (subscribe [:get-entity entity-id])]
    (when @entity
      (if edit?
        [:div.entity.edit
         ^{:key (@entity :id)}
         [entity-editor-view @entity]]
        [:div.entity
         [:a {:href (routes/entity-edit-path {:id (@entity :id)})}
          "Edit"]
         [entity-view @entity]]))))

(defn search-view []
  (let [query (subscribe [:query])
        results (subscribe [:results])]
    [:div.search
     [:input {:value @query
              :on-change (fn [e]
                           (dispatch [:set-query (.. e -target -value)]))}]
     [:div.results
      (let [grouped-results (group-by :type @results)]
        (for [[type results] grouped-results]
          ^{:key type}
          [:div.group
           [:h2 (str type)]
           (for [result results]
             ^{:key (result :id)}
             [:a.result
              {:href (routes/entity-path {:id (result :id)})}
              [search-result-view result]])]))]]))

(defn app-view []
  (let [page @(subscribe [:page])]
    [:div.app
     [styles-view]
     [search-view]
     (when (and page (= (:type page) :entity))
       [active-entity-view (page :id) (page :edit?)])]))
