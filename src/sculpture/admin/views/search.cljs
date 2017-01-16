(ns sculpture.admin.views.search
  (:require
    [clojure.string :as string]
    [sculpture.admin.state.core :refer [subscribe dispatch!]]
    [sculpture.admin.views.search-result :refer [search-result-view]]))

(defn query-view []
  [:div.query
   [:input {:value @(subscribe [:query])
            :on-change (fn [e]
                         (dispatch! [:set-query (.. e -target -value)]))}]])

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
        (for [result results]
          ^{:key (result :id)}
          [search-result-view result])]))])

(defn search-view []
  [:div.search
   [query-view]
   [results-view]])
