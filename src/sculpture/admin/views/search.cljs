(ns sculpture.admin.views.search
  (:require
    [clojure.string :as string]
    [sculpture.admin.state.core :refer [subscribe dispatch!]]
    [sculpture.admin.views.entity.partials.list :refer [entity-list-view]]))

(defn query-view []
  (let [query @(subscribe [:query])]
    [:div.query
     [:input {:placeholder "Search Sculpture"
              :value query
              :on-change (fn [e]
                           (dispatch! [:set-query (.. e -target -value)]))}]
     (when (seq query)
       [:button.clear
        {:on-click (fn [_]
                     (dispatch! [:set-query ""]))}
        "X"])]))

(defn results-view []
  [:div.results
   (let [results (subscribe [:results])
         grouped-results (group-by :type @results)]
     (for [[type results] grouped-results]
       ^{:key type}
       [:div.group {:class (str type)}
        [:h2 (-> (str type)
                 (string/capitalize)
                 (str "s"))]
        [entity-list-view results]]))])

(defn search-view []
  [:div.search
   [query-view]
   [results-view]])
