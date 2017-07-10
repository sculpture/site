(ns sculpture.admin.views.sidebar.entity.sculpture-tag
  (:require
    [sculpture.admin.state.core :refer [subscribe]]
    [sculpture.admin.views.sidebar.entity :refer [entity-view]]
    [sculpture.admin.views.sidebar.entity.partials.photo-mosaic :refer [photo-mosaic-view]]
    [sculpture.admin.views.sidebar.entity.partials.related-sculptures :refer [related-sculptures-view]]))

(defmethod entity-view "sculpture-tag"
  [tag]
  [:div.tag.entity

   [photo-mosaic-view @(subscribe [:sculpture-photos-for
                                   (fn [sculpture]
                                     (contains? (set (sculpture :tag-ids))
                                                (tag :id)))])]
   [:div.info
    [:h1 (tag :name)]
    [:h2 "Sculpture Tag"]]

   [:div.related
    [:h2 "Sculptures"]
    [related-sculptures-view @(subscribe [:sculptures-for
                                          (fn [sculpture]
                                            (contains? (set (sculpture :tag-ids))
                                                       (tag :id)))])]]])
