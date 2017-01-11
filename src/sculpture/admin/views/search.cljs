(ns sculpture.admin.views.search
  (:require
    [clojure.string :as string]
    [re-frame.core :refer [subscribe dispatch]]
    [sculpture.admin.views.search-result :refer [search-result-view]]))

(defn search-view []
  (let [query (subscribe [:query])
        results (subscribe [:results])]
    [:div.search
     [:div.query
      [:input {:value @query
               :on-change (fn [e]
                            (dispatch [:set-query (.. e -target -value)]))}]]
     [:div.results
      (let [grouped-results (group-by :type @results)]
        (for [[type results] grouped-results]
          ^{:key type}
          [:div.group {:class (str type)}
           [:h2 (-> (str type)
                    (string/capitalize)
                    (str "s"))]
           (for [result results]
             ^{:key (result :id)}
             [search-result-view result])]))]]))
