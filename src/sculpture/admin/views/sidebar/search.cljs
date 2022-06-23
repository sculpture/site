(ns sculpture.admin.views.sidebar.search
  (:require
    [clojure.string :as string]
    [sculpture.admin.state.core :refer [subscribe dispatch!]]
    [sculpture.admin.views.sidebar.entity.partials.list :refer [row-view]]))

(defn query-view []
  (let [query @(subscribe [:sculpture.search/query])]
    [:div.query
     [:input {:placeholder "Search Sculpture"
              :default-value query
              :auto-focus true
              :on-focus (fn [_]
                          (dispatch! [:sculpture.search/set-query-focused! true]))
              :on-blur (fn [e]
                         ; set-page does a set-query-focused anyway, so, this is mostly relevant for tabbing
                         ; but, need to have a timeout, b/c otherwise, when user clicks a search result
                         ; the on-blur removes the link before the on-click happens
                         (js/setTimeout (fn [] (dispatch! [:sculpture.search/set-query-focused! false])) 250))
              :on-change (fn [e]
                           (dispatch! [:sculpture.search/set-query! (.. e -target -value)]))}]
     (when (seq query)
       [:button.clear
        {:on-click (fn [_]
                     (dispatch! [:sculpture.search/set-query! ""]))}])]))

(defn results-view []
  [:div.results
   (let [results @(subscribe [:sculpture.search/results])
         grouped-results (group-by :type results)]
     (cond
       (nil? results)
       nil
       (empty? results)
       [:div.no-results "No results"]
       :else
       (for [[type results] grouped-results]
         ^{:key type}
         [:div.group {:class (str type)}
          [:h2 (case (str type)
                 "city" "Cities"
                 "nationality" "Nationalities"
                 "sculpture-tag" "Sculpture Tags"
                 ;; else
                 (-> (str type)
                     (string/capitalize)
                     (str "s")))]
          [:div.entity-list
           (for [result results]
             ^{:key (:id result)}
             [row-view result])]])))])
