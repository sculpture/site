(ns sculpture.admin.views.entity.artist
 (:require
   [re-frame.core :refer [subscribe]]
   [sculpture.admin.views.entity :refer [entity-view]]
   [sculpture.admin.views.entity.partials.related-sculptures :refer [related-sculptures-view]]
   [sculpture.admin.views.entity.partials.related-tags :refer [related-tags-view]]
   [sculpture.admin.views.entity.partials.photo-mosaic :refer [photo-mosaic-view]]))

(defmethod entity-view "artist"
  [artist]
  [:div.artist
   [:div.banner
    [photo-mosaic-view]]
   [:div.info
    [:h1 (artist :name)]
    (artist :gender)]
   [:div.extra
    (when (artist :link-wikipedia)
      [:div.row.wikipedia
       [:a.link {:href (artist :link-wikipedia)} "Wikipedia"]])
    (when (artist :link-website)
      [:div.row.website
       [:a.link {:href (artist :link-website)} "Website"]])
    (when (seq (artist :tag-ids))
      [:div.row.tags
       [related-tags-view (artist :tag-ids)]])
    [:h2 "Sculptures"]
    [related-sculptures-view @(subscribe [:sculptures-for-artist (artist :id)])]]])


