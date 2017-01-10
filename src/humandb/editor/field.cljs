(ns humandb.editor.field
  (:require
    [reagent.core :as r]))

(defmulti field (fn [opts]
                  (opts :type)))

(defmethod field :default
  [entity k _]
  [:div "UNKNOWN FIELD TYPE"])

(defmethod field :string
  [{:keys [length value disabled on-change]}]
  (let [element (case length
                  :long :textarea
                  :short :input
                  :input)]
    [element {:value value
              :disabled disabled
              :on-change (fn [e]
                           (on-change (.. e -target -value)))}]))

(defmethod field :email
  [{:keys [value on-change]}]
  [:input {:type "email"
           :value value
           :on-change (fn [e]
                        (on-change (.. e -target -value)))}])

(defmethod field :url
  [{:keys [value on-change]}]
  [:input {:type "url"
           :value value
           :on-change (fn [e]
                        (on-change (.. e -target -value)))}])

(defmethod field :integer
  [{:keys [value on-change]}]
  [:input {:type "number"
           :value value
           :on-change (fn [e]
(on-change (.. e -target -value)))}])

(defmethod field :enum
  [{:keys [value options on-change]}]
  [:select {:value value
            :on-change (fn [e]
(on-change (.. e -target -value)))}
   (for [option options]
     ^{:key option}
     [:option option])])

(defn related-object-view [object]
  [:div
   (or (object :name)
       (object :title)
       (object :id))])

(defn related-object-existing-view [id on-find]
  (let [object (r/atom nil)
        on-find (or on-find (fn [id cb] (cb nil)))
        _ (on-find id (fn [result]
                        (reset! object result)))]
    (fn []
      (if @object
        [related-object-view @object]
        [:div id]))))

(defmethod field :multi-lookup
  [_]
  (let [show-search? (r/atom false)
        results (r/atom [])]
    (fn [{:keys [value lookup-type on-change on-search on-find]}]
      (let [ids (set value)
            on-search (or on-search (fn [query cb] (cb [])))]
        [:div
         (doall
           (for [id ids]
             ^{:key id}
             [:div
              [related-object-existing-view id on-find]
              [:button {:on-click
                        (fn [_]
                          (on-change (disj ids id)))} "X"]]))
         (if @show-search?
           [:div
            [:input {:placeholder "TODO Search as you type"
                     :on-change (fn [e]
                                  (on-search (.. e -target -value)
                                             (fn [rs]
                                               (reset! results rs))))}]
            [:div
             (when @results
               (for [result (->> @results
                                 (remove (fn [r]
                                           (contains? ids (r :id)))))]
                 ^{:key (result :id)}
                 [:div {:on-click (fn []
                                    (on-change (conj value (result :id)))
                                    (reset! results [])
                                    (reset! show-search? false))}
                  [related-object-view result]]))]
            [:button {:on-click (fn [_]
                                  (reset! show-search? false))}
             "Cancel"]]
           [:button {:on-click (fn [_]
                                 (reset! show-search? true))}
            "+"])]))))

(defmethod field :location
  [{:keys [value on-change]}]
  (let [value (or value {:longitude nil
                         :latitude nil
                         :precision nil})]
    [:div
     [:div {:style {:width "200px"
                    :height "200px"
                    :background "gray"}}
      "MAP"]
     [:div
      "Longitude:"
      [:input {:value (value :longitude)
               :on-change (fn [e]
                            (on-change
                              (assoc value
                                :longitude
                                (.. e -target -value))))}]]
     [:div
      "Latitude:"
      [:input {:value (value :latitude)
               :on-change (fn [e]
                            (assoc value
                              :latitude
                              (.. e -target -value)))}]]
     [:div
      "Precision:"
      [:input {:value (value :precision)
               :on-change (fn [e]
                            (assoc value
                              :precision
                              (.. e -target -value)))}]]]))

(defmethod field :bounds
  [{:keys [value on-change]}]
  (let [bounds (or value {:north nil
                          :east nil
                          :south nil
                          :west nil})]
    [:div
     [:div {:style {:width "200px"
                    :height "200px"
                    :background "gray"}}
      "MAP"]
     (for [[k v] bounds]
       ^{:id k}
       [:div
        k
        [:input {:value v
                 :on-change (fn [e]
                              (on-change
                                (assoc value k
                                  (.. e -target -value))))}]])]))

(defmethod field :geojson
  [{:keys [value on-change]}]
  [:div
   [:div {:style {:width "200px"
                  :height "200px"
                  :background "gray"}}
    "MAP"]])
