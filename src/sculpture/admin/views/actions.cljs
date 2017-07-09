(ns sculpture.admin.views.actions
  (:require
    [sculpture.admin.state.core :refer [dispatch!]]))

(defn actions-view []
  [:div.actions
   [:div.header
    [:h1 "Administrative Actions"]
    [:button.close {:on-click (fn [_]
                                (dispatch! [:set-main-page nil]))}
     "Close"]]
   [:div.content
    [:button
     {:on-click (fn [_]
                  (dispatch! [:sculpture.edit/create-entity {:type "sculpture"}]))}
     "+ Sculpture"]

    [:button
     {:on-click (fn [_]
                  (dispatch! [:sculpture.edit/create-entity {:type "artist"}]))}
     "+ Artist"]

    [:button
     {:on-click (fn [_]
                  (dispatch! [:sculpture.edit/create-entity {:type "region"}]))}
     "+ Region"]

    [:button
     {:on-click (fn [_]
                  (dispatch! [:set-main-page :upload]))}
     "+ Photo"]]])
