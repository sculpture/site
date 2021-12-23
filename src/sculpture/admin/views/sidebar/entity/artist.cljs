(ns sculpture.admin.views.sidebar.entity.artist
 (:require
   [sculpture.admin.state.core :refer [subscribe]]
   [sculpture.admin.views.sidebar.entity :refer [entity-view]]
   [sculpture.admin.views.sidebar.entity.partials.related-sculptures :refer [related-sculptures-view]]
   [sculpture.admin.views.sidebar.entity.partials.related-tags :refer [related-tags-view]]
   [sculpture.admin.views.sidebar.entity.partials.related-nationalities :refer [related-nationalities-view]]
   [sculpture.admin.views.sidebar.entity.partials.photo-mosaic :refer [photo-mosaic-view]]))

(defmethod entity-view "artist"
  [artist]
  [:div.artist.entity
   [photo-mosaic-view @(subscribe [:sculpture-photos-for
                                   (fn [sculpture]
                                     (contains? (set (sculpture :artist-ids))
                                                (artist :id)))])]
   [:div.info
    [:h1 (artist :name)]
    (when (artist :birth-date)
      [:h2 [:div.year
            (artist :birth-date)
            (when (artist :death-date)
              (str "–" (artist :death-date)))]])]
   [:div.meta
    (when (artist :link-wikipedia)
      [:div.row.wikipedia {:title "Wikipedia Link"}
       [:a.link {:href (artist :link-wikipedia)} "Wikipedia"]])
    (when (artist :link-website)
      [:div.row.website {:title "Website Link"}
       [:a.link {:href (artist :link-website)} "Website"]])
    (when (seq (artist :tag-ids))
      [:div.row.tags {:title "Tags"}
       [related-tags-view (artist :tag-ids)]])
    (when (artist :gender)
      [:div.row.gender {:title "Gender"}
       (artist :gender)])
    (when (artist :nationality-ids)
      [:div.row.nationalities {:title "Nationality"}
       [related-nationalities-view (artist :nationality-ids)]])]
   [:div.related
    [:h2 "Sculptures"]
    [related-sculptures-view @(subscribe [:sculptures-for
                                          (fn [sculpture]
                                            (contains? (set (sculpture :artist-ids))
                                                       (artist :id)))])]]])


