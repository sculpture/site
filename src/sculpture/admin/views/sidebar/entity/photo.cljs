(ns sculpture.admin.views.sidebar.entity.photo
  (:require
    [bloom.commons.pages :as pages]
    [sculpture.admin.helpers :as helpers]
    [sculpture.admin.views.sidebar.entity :refer [entity-handler]]
    [sculpture.admin.views.sidebar.entity.partials.photos :refer [photo-view]]))

(defn photo-entity-view
  [photo]
  [:div.photo.entity
   [photo-view {:photo photo
                :size :medium}]

   [:div.meta

    [:div.row.user {:title "Photo by"}
     (let [user (:photo/user photo)]
       [:a {:href (pages/path-for [:page/user {:id (:user/id user)}])}
        (:user/name user)])]

    (when-let [sculpture (:photo/sculpture photo)]
      [:div.row.sculpture {:title "Sculpture"}
       [:a {:href (pages/path-for [:page/sculpture {:id (:sculpture/id sculpture)}])}
        (:sculpture/title sculpture)]
       (when-let [segment (:photo/segment photo)]
         [:<>
          " | "
          [:a {:href (pages/path-for [:page/segment {:id (:segment/id segment)}])} (:segment/name segment)]])])

    [:div.row.captured-at {:title "Captured At"}
     (helpers/format-date (:photo/captured-at photo) "yyyy-MM-dd")]

    [:div.row.dimensions {:title "Dimensions"}
     (:photo/width photo) "px" " × " (:photo/height photo) "px"]

    [:div.row.colors {:title "Colors"}
     (for [color (:photo/colors photo)]
       ^{:key color}
       [:div.color
        [:div.swatch {:style {:background-color color
                              :width "1em"
                              :height "1em"
                              :display "inline-block"}}]
        [:div.name color]])]]])

(defmethod entity-handler :photo
  [_ photo-id]
  {:identifier {:photo/id photo-id}
   :pattern [:photo/id
             :photo/captured-at
             :photo/width
             :photo/height
             :photo/colors
             {:photo/sculpture
              [:sculpture/id
               :sculpture/title]}
             {:photo/segment
              [:segment/id
               :segment/name]}
             {:photo/user
              [:user/id
               :user/name]}]
   :view photo-entity-view})
