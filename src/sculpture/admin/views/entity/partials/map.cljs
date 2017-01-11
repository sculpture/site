(ns sculpture.admin.views.entity.partials.map)

(defn map-view
  [location]
  [:div.map {:style {:width "200px"
                     :height "50px"
                     :background "#CCC"}}
   (location :latitude)
   (location :longitude)])
