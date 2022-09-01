(ns sculpture.admin.views.pages.regions
  (:require
    [reagent.core :as r]
    [sculpture.admin.state.api :refer [dispatch!]]))

(defn regions-view []
  (r/with-let [regions (r/atom [])
               _ (dispatch! [:state.core/remote-eql!
                             :regions
                             [:region/id
                              :region/name
                              :region/points-count
                              :region/area
                              :region/geojson]
                             (fn [data]
                               (reset! regions data))])]
      [:div.page.regions
       [:div.header
        [:h1 "Regions"]
        [:button.close {:on-click (fn [_]
                                    (dispatch! [:state.core/set-main-page! nil]))}
         "Close"]]
       (when (seq @regions)
         [:div.content
          [:button {:on-click (fn [_]
                                (dispatch! [:state.mega-map/show!
                                            (->> @regions
                                                 (map
                                                   (fn [region]
                                                     (when (:region/geojson region)
                                                       {:type :geojson
                                                        :bound? false
                                                        :geojson (js/JSON.parse (:region/geojson region))})))
                                                 (remove nil?))]))}
           "Show Regions on Map"]
          [:table
           [:thead
            [:tr
             [:th "Name"]
             [:th "Points #"]
             [:th "Area"]]]
           [:tbody
            (for [region (->> @regions
                              (sort-by (fn [region]
                                         (:region/area region)))
                              reverse)]
              ^{:key (:region/id region)}
              [:tr
               [:td (:region/name region)]
               [:td (:region/points-count region)]
               [:td (:region/area region)]])]]])]))

