(ns sculpture.admin.views.entity.photo
  (:require
    [cljs-time.format :as f]
    [cljs-time.coerce :as c]
    [sculpture.admin.state.core :refer [subscribe]]
    [sculpture.admin.routes :as routes]
    [sculpture.admin.views.entity :refer [entity-view]]
    [sculpture.admin.views.object :refer [object-view]]
    [sculpture.admin.views.entity.partials.photos :refer [photo-view]]))

(defmethod entity-view "photo"
  [photo]
  [:div.photo
   [:div.banner
    [photo-view {:photo photo
                 :size :medium}]]
   ;[:div.info]
   [:div.extra

    [:div.row.user
     (let [user @(subscribe [:get-entity (photo :user-id)])]
       [:a {:href (routes/entity-path {:id (user :id)})}
        (user :name)])]

    [:div.row.sculpture
     (let [sculpture @(subscribe [:get-entity (photo :sculpture-id)])]
       [:a {:href (routes/entity-path {:id (sculpture :id)})}
        (sculpture :title)])]

    [:div.row.captured-at
     (f/unparse
       (f/formatter "yyyy-MM-dd" )
       (c/from-date (photo :captured-at)))]]])
