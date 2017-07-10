(ns sculpture.admin.views.sidebar.entity.material
  (:require
    [sculpture.admin.state.core :refer [subscribe]]
    [sculpture.admin.views.sidebar.entity :refer [entity-view]]
    [sculpture.admin.views.sidebar.entity.partials.photo-mosaic :refer [photo-mosaic-view]]
    [sculpture.admin.views.sidebar.entity.partials.related-sculptures :refer [related-sculptures-view]]))

(defmethod entity-view "material"
  [material]
  [:div.material.entity

   [photo-mosaic-view @(subscribe [:sculpture-photos-for
                                   (fn [sculpture]
                                     (contains? (set (sculpture :material-ids))
                                                (material :id)))])]
   [:div.info
    [:h1 (material :name)]
    [:h2 "Material"]]

   [:div.related
    [:h2 "Sculptures"]
    [related-sculptures-view @(subscribe [:sculptures-for
                                          (fn [sculpture]
                                            (contains? (set (sculpture :material-ids))
                                                       (material :id)))])]]])
