(ns sculpture.admin.views.sidebar.search
  (:require
    [reagent.core :as r]
    [clojure.string :as string]
    [sculpture.admin.state.api :refer [subscribe dispatch!]]
    [sculpture.admin.views.sidebar.entity.partials.list :refer [row-view]]))

(defn query-view []
  (r/with-let [;; with on-blur, when a user clicks a search result
               ;; on-blur removes the link before the on-click happens
               ;; this pseudo-on-blur happens after the on-click (bubbles to window)
               pseudo-on-blur (fn [e]
                                (when (not= (.. e -target -id) "search")
                                  (dispatch! [:state.search/set-query-focused! false])))
               _ (..  js/window (addEventListener "click" pseudo-on-blur false))]
    (let [query @(subscribe [:state.search/query])]
      [:div.query
       [:input {:placeholder "Search Sculpture"
                :default-value query
                :auto-focus true
                :id "search"
                :on-focus (fn [_]
                            (dispatch! [:state.search/set-query-focused! true]))
                :on-change (fn [e]
                             (dispatch! [:state.search/set-query! (.. e -target -value)]))}]
       (when (seq query)
         [:button.clear
          {:on-click (fn [_]
                       (dispatch! [:state.search/set-query! ""]))}])])
    (finally
      (.. js/window (removeEventListener "click" pseudo-on-blur false)))))

(defn results-view []
  [:div.results
   (let [results @(subscribe [:state.search/results])
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
