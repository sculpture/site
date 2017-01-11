(ns sculpture.admin.views.app
  (:require
    [re-frame.core :refer [subscribe dispatch]]
    [sculpture.admin.views.search-result :refer [search-result-view]]
    [sculpture.admin.views.styles :refer [styles-view]]
    [sculpture.admin.views.entity :refer [entity-view]]
    [sculpture.admin.views.entity.sculpture]
    [sculpture.admin.views.entity-editor :refer [entity-editor-view]]))

(defn active-entity-view []
  (let [entity (subscribe [:active-entity])
        edit? (subscribe [:edit?])]
    (when @entity
      (if @edit?
        [:div.entity.edit
         ^{:key (@entity :id)}
         [entity-editor-view @entity]]
        [:div.entity
         [:button {:on-click (fn []
                               (dispatch [:edit]))}
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
              {:href ""
               :on-click (fn [e]
                           (.preventDefault e)
                           (dispatch [:set-active-entity-id (result :id)]))}
              [search-result-view result]])]))]]))

(defn app-view []
  [:div.app
   [styles-view]
   [search-view]
   [active-entity-view]])
