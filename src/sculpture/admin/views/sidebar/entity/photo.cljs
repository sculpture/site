(ns sculpture.admin.views.sidebar.entity.photo
  (:require
    [bloom.commons.pages :as pages]
    [sculpture.admin.helpers :as helpers]
    [sculpture.admin.state.core :refer [subscribe]]
    [sculpture.admin.views.sidebar.entity :refer [entity-view]]
    [sculpture.admin.views.sidebar.entity.partials.photos :refer [photo-view]]))

(defmethod entity-view "photo"
  [photo]
  [:div.photo.entity
   [photo-view {:photo photo
                :size :medium}]

   [:div.meta

    [:div.row.user {:title "Photo by"}
     (let [user @(subscribe [:get-entity (photo :user-id)])]
       [:a {:href (pages/path-for [:page/user {:id (user :id)}])}
        (user :name)])]

    (when-let [sculpture @(subscribe [:get-entity (photo :sculpture-id)])]
      [:div.row.sculpture {:title "Sculpture"}
       [:a {:href (pages/path-for [:page/sculpture {:id (sculpture :id)}])}
        (sculpture :title)]])

    [:div.row.captured-at {:title "Capture At"}
     (helpers/format-date (photo :captured-at) "yyyy-MM-dd")]

    [:div.row.dimensions {:title "Dimensions"}
     (photo :width) "px" " × " (photo :height) "px"]

    [:div.row.colors {:title "Colors"}
     (for [color (photo :colors)]
       ^{:key color}
       [:div.color
        [:div.swatch {:style {:background-color color
                              :width "1em"
                              :height "1em"
                              :display "inline-block"}}]
        [:div.name color]])]]])
