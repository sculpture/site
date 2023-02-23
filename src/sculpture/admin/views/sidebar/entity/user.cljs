(ns sculpture.admin.views.sidebar.entity.user
  (:require
    [bloom.commons.pages :as pages]
    [sculpture.admin.views.sidebar.entity :refer [entity-handler]]
    [sculpture.admin.views.sidebar.entity.partials.photo-mosaic :refer [photo-mosaic-view]]
    [sculpture.admin.views.sidebar.entity.partials.list :refer [entity-list-view]]))

(defn user-entity-view
  [user]
  [:div.user.entity
   [photo-mosaic-view (->> (:user/photos user)
                           (map (fn [photo]
                                  {:link (pages/path-for [:page/photo {:id (:photo/id photo)}])
                                   :photo photo})))]
   [:div.info
    [:h1 (:user/name user)]]])

(defmethod entity-handler :user
  [_ user-id]
  {:identifier {:user/id user-id}
   :pattern [:user/id
             :user/name
             {:user/photos
              [:photo/id
               :photo/width
               :photo/height
               :photo/colors]}]
   :view user-entity-view})
