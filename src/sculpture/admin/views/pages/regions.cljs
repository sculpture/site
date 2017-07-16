(ns sculpture.admin.views.pages.regions
  (:require
    [sculpture.admin.state.core :refer [subscribe dispatch!]]
    [sculpture.geo :as geo]))

(defn regions-view []
  (let [regions @(subscribe [:sculpture.regions/all])]
    [:div.page.regions
     [:div.header
      [:h1 "Regions"]
      [:button.close {:on-click (fn [_]
                                  (dispatch! [:set-main-page nil]))}
       "Close"]]
     [:div.content
      [:button {:on-click (fn [_]
                            (dispatch! [:sculpture.mega-map/show
                                        (->> regions
                                             (map
                                               (fn [region]
                                                 (when (region :geojson)
                                                   {:type :geojson
                                                    :bound? false
                                                    :geojson (js/JSON.parse (region :geojson))})))
                                             (remove nil?))]))}
       "Show Regions on Map"]
      [:table
       [:thead
        [:tr
         [:th "Name"]
         [:th "Points #"]
         [:th "Area"]]]
       [:tbody
        (for [region (->> regions
                          (map (fn [region]
                                 (let [coordinates (geo/geojson->coords (region :geojson))]
                                   (assoc region
                                     :coordinates coordinates
                                     :point-count (-> coordinates first count)
                                     :area (js/Math.floor (/ (geo/ring-area (first coordinates))
                                                             1000000))))))
                          (sort-by (fn [region]
                                     (region :area)))
                          reverse)]
          ^{:key (region :id)}
          [:tr
           [:td (region :name)]
           [:td (region :point-count)]
           [:td (region :area)]])]]]]))

