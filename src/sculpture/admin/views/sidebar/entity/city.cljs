(ns sculpture.admin.views.sidebar.entity.city
  (:require
    [sculpture.admin.state.core :refer [subscribe]]
    [sculpture.admin.views.sidebar.entity :refer [entity-view]]
    [sculpture.admin.views.sidebar.entity.partials.photo-mosaic :refer [photo-mosaic-view]]
    [sculpture.admin.views.sidebar.entity.partials.related-sculptures :refer [related-sculptures-view]]))

(defmethod entity-view "city"
  [city]
  [:div.city.entity

   [photo-mosaic-view @(subscribe [:sculpture-photos-for
                                   (fn [sculpture]
                                     (= (sculpture :city-id) (city :id)))])]

   [:div.info
    [:h1 (city :city) ", " (city :region) ", " (city :country)]
    [:h2 "City"]]

   [:div.related
    [:h2 "Sculptures"]
    [related-sculptures-view @(subscribe [:sculptures-for
                                          (fn [sculpture]
                                            (= (sculpture :city-id) (city :id)))])]]])
