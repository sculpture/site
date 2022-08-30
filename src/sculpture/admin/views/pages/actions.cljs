(ns sculpture.admin.views.pages.actions
  (:require
    [sculpture.admin.state.api :refer [dispatch!]]))

(defn actions-view []
  [:div.actions
   [:div.header
    [:h1 "Administrative Actions"]
    [:button.close {:on-click (fn [_]
                                (dispatch! [:state.core/set-main-page! nil]))}
     "Close"]]

   [:div.content

    [:button
     {:on-click (fn [_]
                  (dispatch! [:state.auth/log-out!]))}
     "Log Out"]

    [:button
     {:on-click (fn [_]
                  (dispatch! [:state.core/set-main-page! :main-page/advanced-search]))}
     "Advanced Search"]

    [:button
     {:on-click (fn [_]
                  (dispatch! [:state.advanced-search/go!
                              "photo"
                              [{:key :photo/sculpture-id
                                :option :nil?
                                :value nil}]]))}
     "Photos w/ no Sculpture"]

    [:button
     {:on-click (fn [_]
                  (dispatch! [:state.core/set-main-page! :main-page/regions]))}
     "Regions"]



    [:h2 "Create New"]

    [:button
     {:on-click (fn [_]
                  (dispatch! [:state.edit/create-entity! {:type "sculpture"}]))}
     "+ Sculpture"]

    [:button
     {:on-click (fn [_]
                  (dispatch! [:state.edit/create-entity! {:type "artist"}]))}
     "+ Artist"]

    [:button
     {:on-click (fn [_]
                  (dispatch! [:state.edit/create-entity! {:type "city"}]))}
     "+ City"]

    [:button
     {:on-click (fn [_]
                  (dispatch! [:state.edit/create-entity! {:type "region"}]))}
     "+ Region"]

    [:button
     {:on-click (fn [_]
                  (dispatch! [:state.core/set-main-page! :main-page/upload]))}
     "+ Photo"]

    [:button
     {:on-click (fn [_]
                  (dispatch! [:state.edit/create-entity! {:type "material"}]))}
     "+ Material"]

    [:button
     {:on-click (fn [_]
                  (dispatch! [:state.edit/create-entity! {:type "sculpture-tag"}]))}
     "+ Sculpture Tag"]

    [:button
     {:on-click (fn [_]
                  (dispatch! [:state.edit/create-entity! {:type "nationality"}]))}
     "+ Nationality"]

    [:button
     {:on-click (fn [_]
                  (dispatch! [:state.edit/create-entity! {:type "artist-tag"}]))}
     "+ Artist Tag"]

    [:button
     {:on-click (fn [_]
                  (dispatch! [:state.edit/create-entity! {:type "region-tag"}]))}
     "+ Region Tag"]]])
