(ns sculpture.admin.views.pages.advanced-search
  (:require
    [bloom.commons.pages :as pages]
    [humandb.ui.field :refer [field]]
    [sculpture.admin.state.api :refer [dispatch! subscribe]]
    [sculpture.schema.util :as schema.util]
    [sculpture.schema.schema :as schema]))

(defn option->field-opts [option key]
  (case option
    :equals? (schema.util/attr->input key)
    :nil? nil
    :re-matches? {:type :string}
    :includes? {:type :string}
    :before? {:type :date}
    :after? {:type :date}
    :less-than? {:type :integer}
    :greater-than? {:type :integer}
    :empty? nil
    :contains? (merge (schema.util/attr->input key)
                      {:type :single-lookup})
    nil))

(def key-type->options
  {:string #{:nil? :equals? :includes? :re-matches?}
   :enum #{:nil? :equals?}
   :date #{:nil? :equals? :before? :after?}
   :datetime #{:nil? :equals? :before? :after?}
   :geojson #{:nil?}
   :url #{:nil? :equals? :re-matches?}
   :integer #{:nil? :equals? :less-than? :greater-than?}
   :location #{:nil?}
   :single-lookup #{:nil? :empty?}
   :multi-lookup #{:nil? :empty? :contains?}})

(defn advanced-search-view []
  (let [entity-type @(subscribe [:state.advanced-search/entity-type])
        conditions @(subscribe [:state.advanced-search/conditions])
        results @(subscribe [:state.advanced-search/results])]
    [:div.page.advanced-search
     [:div.header
      [:h1 "Advanced Search"]
      [:button.close {:on-click (fn [_]
                                  (dispatch! [:state.advanced-search/clear!])
                                  (dispatch! [:state.core/set-main-page! nil]))}
       "Close"]]

     [:div.content
      [:h2 "Entity Type"]
      [:select {:value (or entity-type "")
                :on-change (fn [e]
                             (dispatch! [:state.advanced-search/set-entity-type! (.. e -target -value)]))}
       [:option {:value nil} ""]
       (for [entity-type schema/entity-types]
         ^{:key entity-type}
         [:option {:value entity-type} entity-type])]

      [:h2 "Conditions"]
      [:div.conditions
       (->> conditions
            (map-indexed
              (fn [index condition]
                ^{:key index}
                [:div.condition

                 [:div.key
                  (let [key-string->key (zipmap (map str (schema.util/keys-of-all-entities))
                                                (schema.util/keys-of-all-entities))]
                    [:select {:value (str (condition :key))
                              :on-change (fn [e]
                                           (dispatch! [:state.advanced-search/update-condition! index :key (key-string->key (.. e -target -value))])
                                           (dispatch! [:state.advanced-search/update-condition! index :option nil])
                                           (dispatch! [:state.advanced-search/update-condition! index :value nil]))}
                     [:option {:value ""} ""]
                     (for [entity-key (sort (schema.util/entity-keys entity-type))]
                       ^{:key entity-key}
                       [:option {:value (str entity-key)} (str entity-key)])])]

                 [:div.option
                  (when (condition :key)
                    (let [key-type (:type (schema.util/attr->input (condition :key)))
                          options (key-type->options key-type)]
                      [:select {:value (condition :option)
                                :on-change (fn [e]
                                             (dispatch! [:state.advanced-search/update-condition! index :option (keyword (.. e -target -value))])
                                             (dispatch! [:state.advanced-search/update-condition! index :value nil]))}
                       [:option {:value ""} ""]
                       (for [option options]
                         ^{:key option}
                         [:option {:value option} (name option)])]))]

                 [:div.value
                  (when (and (condition :key) (condition :option))
                    (when-let [field-opts (option->field-opts (condition :option) (condition :key))]
                      [field (merge field-opts
                                    {:value (condition :value)
                                     :disabled false
                                     :on-change (fn [v]
                                                  (dispatch! [:state.advanced-search/update-condition! index :value v]))})]))]

                 [:button {:on-click (fn [_]
                                       (dispatch! [:state.advanced-search/remove-condition! index]))} "×"]])))

       [:button {:on-click (fn [_]
                             (dispatch! [:state.advanced-search/add-condition!]))}
        "+"]]

      [:button
       {:on-click (fn [_] (dispatch! [:state.advanced-search/search!]))}
       "Search"]

      (when (seq results)
        [:div.results
         [:div (count results) " Results"]
         (let [first-result (first results)
               entity-type (schema.util/entity-type first-result)
               ks (schema.util/entity-keys entity-type)]
           [:table
            [:thead
             [:tr
              (for [k ks]
                ^{:key k}
                [:th {:style {:white-space "nowrap"}} (str k)])]]
            [:tbody
             (for [entity results]
               (let [id ((keyword entity-type "id") entity)]
                 ^{:key id}
                 [:tr
                  [:td
                   [:a {:href (pages/path-for [(keyword "page" entity-type)
                                               {:id id}])} id]]
                  (for [k (rest ks)]
                    ^{:key k}
                    [:td (str (get entity k))])]))]])])]]))
