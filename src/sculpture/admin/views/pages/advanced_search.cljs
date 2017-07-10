(ns sculpture.admin.views.pages.advanced-search
  (:require
    [sculpture.admin.state.core :refer [dispatch!]]))

(defn advanced-search-view []
  [:div.page.advanced-search
   [:div.header
    [:h1 "Advanced Search"]
    [:button.close {:on-click (fn [_]
                                (dispatch! [:set-main-page nil]))}
     "Close"]]])
