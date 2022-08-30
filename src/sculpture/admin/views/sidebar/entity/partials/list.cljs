(ns sculpture.admin.views.sidebar.entity.partials.list
  (:require
    [clojure.string :as string]
    [bloom.commons.pages :as pages]
    [sculpture.admin.pages :refer [entity-type->page-id]]
    [sculpture.admin.helpers :as helpers]
    [sculpture.admin.views.sidebar.entity.partials.photos :refer [photo-view]]))

(defmulti entity-row-data (fn [entity]
                           (or (:type entity)
                               (cond
                                 (entity :artist/id) "artist"
                                 (entity :sculpture/id) "sculpture"))))

(defmethod entity-row-data "city"
  [city]
  {:title (city :city)
   :subtitle (str (city :region) ", " (city :country))
   :id (city :id)
   :photo-id nil
   :type "city"})

(defmethod entity-row-data "photo"
  [photo]
  {:title "Photo"
   :subtitle (helpers/format-date (photo :captured-at) "yyyy-MM-dd")
   :id (photo :id)
   :photo-id (:id photo)
   :type "photo"})

(defmethod entity-row-data "sculpture"
  [sculpture]
  {:title (:sculpture/title sculpture)
   :subtitle (string/join ", " (map :artist/name (:sculpture/artists sculpture)))
   :id (:sculpture/id sculpture)
   :photo-id (:photo/id (first (:sculpture/photos sculpture)))
   :type "sculpture"})

(defmethod entity-row-data :default
  [entity]
  {:title (or (entity :title) (entity :name))
   :subtitle "..."
   :id (entity :id)
   :photo-id nil
   :type (entity :type)})

(defn row-view
  [{:keys [id photo-id type title subtitle]}]
  [:a.entity
   {:href (pages/path-for [(entity-type->page-id type) {:id id}])
    :class type}
   [photo-view {:photo {:photo/id photo-id}
                :size :thumb}]
   [:div.h1 title]
   [:div.h2 subtitle]])

(defn entity-row-view [entity]
  (let [data (entity-row-data entity)]
    [row-view data]))

(defn entity-list-view [entities]
  [:div.entity-list
   (for [entity entities]
     ^{:key (or (:id entity)
                (:sculpture/id entity))}
     [entity-row-view entity])])
