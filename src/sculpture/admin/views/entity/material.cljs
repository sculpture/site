(ns sculpture.admin.views.entity.material
  (:require
    [sculpture.admin.state.core :refer [subscribe]]
    [sculpture.admin.views.entity :refer [entity-view]]
    [sculpture.admin.views.entity.partials.photo-mosaic :refer [photo-mosaic-view]]
    [sculpture.admin.views.entity.partials.related-sculptures :refer [related-sculptures-view]]))

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
