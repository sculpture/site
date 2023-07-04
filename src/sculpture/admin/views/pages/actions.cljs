(ns sculpture.admin.views.pages.actions
  (:require
    [bloom.commons.uuid :as uuid]
    [sculpture.schema.schema :as schema]
    [sculpture.admin.state.api :refer [dispatch!]]))

(defn actions-view []
  [:div.page.actions
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

    (for [entity-type (->> schema/entities
                           (map :entity/id)
                           (remove #{"user" "photo"})
                           (sort))]
      ^{:key entity-type}
      [:button
       {:on-click (fn [_]
                    (dispatch! [:state.edit/create-entity!
                                {(keyword entity-type "id") (uuid/random)}]))}
       "+ " entity-type])

    [:button
     {:on-click (fn [_]
                  (dispatch! [:state.core/set-main-page! :main-page/upload]))}
     "+ Photo"]]])
