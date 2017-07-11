(ns sculpture.admin.views.sidebar.entity.photo
  (:require
    [sculpture.admin.helpers :as helpers]
    [sculpture.admin.state.core :refer [subscribe]]
    [sculpture.admin.routes :as routes]
    [sculpture.admin.views.sidebar.entity :refer [entity-view]]
    [sculpture.admin.views.sidebar.entity.partials.photos :refer [photo-view]]))

(defmethod entity-view "photo"
  [photo]
  [:div.photo.entity
   [photo-view {:photo photo
                :size :medium}]

   [:div.meta

    [:div.row.user
     (let [user @(subscribe [:get-entity (photo :user-id)])]
       [:a {:href (routes/entity-path {:id (user :id)})}
        (user :name)])]

    (when-let [sculpture @(subscribe [:get-entity (photo :sculpture-id)])]
      [:div.row.sculpture
       [:a {:href (routes/entity-path {:id (sculpture :id)})}
        (sculpture :title)]])

    [:div.row.captured-at
     (helpers/format-date (photo :captured-at) "yyyy-MM-dd")]

    [:div.row.dimensions
     (photo :width) "px" " × " (photo :height) "px"]

    [:div.row.colors
     (for [color (photo :colors)]
       ^{:key color}
       [:div.color
        [:div.swatch {:style {:background-color color
                              :width "1em"
                              :height "1em"
                              :display "inline-block"}}]
        [:div.name color]])]]])
