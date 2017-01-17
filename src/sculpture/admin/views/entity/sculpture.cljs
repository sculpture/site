(ns sculpture.admin.views.entity.sculpture
  (:require
    [clojure.string :as string]
    [sculpture.admin.routes :as routes]
    [sculpture.admin.state.core :refer [subscribe dispatch!]]
    [sculpture.admin.views.entity :refer [entity-view]]
    [sculpture.admin.views.entity.partials.photos :refer [photo-view]]
    [sculpture.admin.views.entity.partials.map :refer [map-view]]
    [sculpture.admin.views.entity.partials.related-tags :refer [related-tags-view]]
    [sculpture.admin.views.entity.partials.related-materials :refer [related-materials-view]]
    [sculpture.admin.views.entity.partials.related-artists :refer [related-artists-view]]))

(defn photos-view [sculpture-id]
  (let [photos (subscribe [:photos-for-sculpture sculpture-id])]
    [:div.photos
     (for [photo @photos]
       ^{:key (photo :id)}
       [photo-view {:photo photo
                    :size :large}])]))

(defmethod entity-view "sculpture"
  [sculpture]
  [:div.sculpture
   [:div.banner
    [photos-view (sculpture :id)]]

   [:div.info
    [:h1 (sculpture :title)]
    [:h2
     (let [artists @(subscribe [:get-entities (sculpture :artist-ids)])]
       (into [:div.artists]
             (interpose
               [:span ", "]
               (for [artist artists]
                 [:a {:href (routes/entity-path {:id (artist :id)})} (artist :name)]))))

     [:div.year (sculpture :year)]]]

   [:div.extra
    (when (seq (sculpture :tag-ids))
      [:div.row.tags
       [related-tags-view (sculpture :tag-ids)]])

    (when (seq (sculpture :material-ids))
      [:div.row.materials
       [related-materials-view (sculpture :material-ids)]])

    (when (sculpture :location)
      [:div.row.location
       [:a {:href "#"
            :on-click (fn [e]
                        (.preventDefault e)
                        (dispatch! [:sculpture.mega-map/go-to (sculpture :location)]))}
        (-> (sculpture :location)
            (select-keys [:longitude :latitude])
            vals
            (->>
              (map (fn [c] (/ (js/Math.round (* c 1000)) 1000)))
              (string/join ", " )))]])

    (when (seq (sculpture :commissioned-by))
      [:div.row.commission
       (sculpture :commissioned-by)])

    (when (seq (sculpture :note))
      [:div.row.note (sculpture :note)])

    #_(when (sculpture :location)
        [map-view {:disable-interaction? true
                   :on-click (fn [_]
                               (dispatch! [:sculpture.mega-map/go-to (sculpture :location)]))
                   :center (sculpture :location)
                   :shapes [{:location (sculpture :location)
                             :type :icon}]}])]])
