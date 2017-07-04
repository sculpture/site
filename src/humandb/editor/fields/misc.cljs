(ns humandb.editor.fields.misc
  (:require
    [cljs-time.format :as f]
    [cljs-time.coerce :as c]
    [reagent.core :as r]
    [humandb.editor.fields.core :refer [field]]))

(defmethod field :default
  [entity k _]
  [:div "UNKNOWN FIELD TYPE"])

(defmethod field :date
  [{:keys [value on-change]}]
  [:input {:type "date"
           :value (when value
                    (f/unparse
                      (f/formatter "yyyy-MM-dd")
                      (c/from-date value)))
           :on-change (fn [e]
                        (on-change (c/to-date
                                     (f/parse
                                       (f/formatter "yyyy-MM-dd")
                                       (.. e -target -value)))))}])

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
     ^{:key (str option)}
     [:option {:value option} (str option)])])

(defn related-object-view [object]
  [:div.related
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
    (fn [{:keys [value on-change on-search on-find]}]
      (let [ids (set value)
            on-search (or on-search (fn [query cb] (cb [])))]
        [:div.multi.lookup
         (doall
           (for [id ids]
             ^{:key id}
             [:div.value
              [related-object-existing-view id on-find]
              [:button.remove {:on-click
                               (fn [_]
                                 (on-change (disj ids id)))}]]))
         (if @show-search?
           [:div.search
            [:input {:placeholder "Search"
                     :on-change (fn [e]
                                  (on-search (.. e -target -value)
                                             (fn [rs]
                                               (reset! results rs))))}]
            [:div.results
             (when @results
               (for [result (->> @results
                                 (remove (fn [r]
                                           (contains? ids (r :id)))))]
                 ^{:key (result :id)}
                 [:div.result {:on-click (fn []
                                           (on-change (conj value (result :id)))
                                           (reset! results [])
                                           (reset! show-search? false))}
                  [related-object-view result]]))]
            [:button.cancel {:on-click (fn [_]
                                         (reset! show-search? false))}]]
           [:button.plus {:on-click (fn [_]
                                      (reset! show-search? true))}])]))))


(defmethod field :single-lookup
  [_]
  (let [results (r/atom [])]
    (fn [{:keys [value on-change on-find on-search]}]
      [:div.single.lookup
       (if value
         [:div.value
          [related-object-existing-view value on-find]
          [:button.remove {:on-click
                           (fn [_]
                             (on-change nil))}]]
         [:div.search
          [:input {:placeholder "Search"
                   :on-change (fn [e]
                                (on-search (.. e -target -value)
                                           (fn [rs]
                                             (reset! results rs))))}]
          [:div.results
           (when @results
             (for [result @results]
               ^{:key (result :id)}
               [:div.result {:on-click (fn []
                                         (on-change (result :id))
                                         (reset! results []))}
                [related-object-view result]]))]])])))
