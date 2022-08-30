(ns sculpture.admin.views.sidebar.entity.sculpture-tag
  (:require
    [bloom.commons.pages :as pages]
    [sculpture.admin.views.sidebar.entity :refer [entity-handler]]
    [sculpture.admin.views.sidebar.entity.partials.photo-mosaic :refer [photo-mosaic-view]]
    [sculpture.admin.views.sidebar.entity.partials.related-sculptures :refer [related-sculptures-view]]))

(defn sculpture-tag-entity-view
  [tag]
  [:div.tag.entity
   [photo-mosaic-view (->> (:sculpture-tag/sculptures tag)
                           (mapcat :sculpture/photos))]

   [:div.info
    [:h1 (:sculpture-tag/name tag)]
    [:h2 "Sculpture Tag"]]
   [:div.meta
    [:div.row.category
     [:a.link {:href (pages/path-for [:page/category {:id (:category/id (:sculpture-tag/category tag))}])}
      (:category/name (:sculpture-tag/category tag))]]]
   [:div.related
    [:h2 "Sculptures"]
    [related-sculptures-view (:sculpture-tag/sculptures tag)]]])

(defmethod entity-handler :sculpture-tag
  [_ sculpture-tag-id]
  {:identifier {:sculpture-tag/id sculpture-tag-id}
   :pattern [:sculpture-tag/id
             :sculpture-tag/name
             {:sculpture-tag/category [:category/id
                                       :category/name]}
             {:sculpture-tag/sculptures
              [:sculpture/id
               :sculpture/title
               {:sculpture/artists
                [:artist/name]}
               {:sculpture/photos
                [:photo/id
                 :photo/width
                 :photo/height
                 :photo/colors]}]}]
   :view sculpture-tag-entity-view})
