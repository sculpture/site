(ns sculpture.admin.views.pages.advanced-search
  (:require
    [humandb.ui.field :refer [field]]
    [sculpture.admin.state.core :refer [dispatch! subscribe]]
    [sculpture.admin.views.pages.entity-editor :refer [schema]]
    [sculpture.admin.views.sidebar.entity.partials.list :refer [entity-row-view]]))

(defn option->field-opts [option key]
  (case option
    :equals? (schema key)
    :nil? nil
    :re-matches? {:type :string}
    :includes? {:type :string}
    :before? {:type :date}
    :after? {:type :date}
    :less-than? {:type :integer}
    :greater-than? {:type :integer}
    :empty? nil
    :contains? (merge (schema key)
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
  [:div.page.advanced-search
   [:div.header
    [:h1 "Advanced Search"]
    [:button.close {:on-click (fn [_]
                                (dispatch! [:set-main-page nil]))}
     "Close"]]

   [:div.content
    [:div.conditions
     (map-indexed
       (fn [index condition]
         ^{:key index}
         [:div.condition

          [:div.key
           [:select {:value (condition :key)
                     :on-change (fn [e]
                                  (dispatch! [:sculpture.advanced-search/update-condition index :key (keyword (.. e -target -value))])
                                  (dispatch! [:sculpture.advanced-search/update-condition index :option nil])
                                  (dispatch! [:sculpture.advanced-search/update-condition index :value nil]))}
            [:option {:value ""} ""]
            (for [entity-key (sort (keys schema))]
              ^{:key entity-key}
              [:option {:value entity-key} (name entity-key)])]]

          [:div.option
           (when (condition :key)
             (let [key-type (get-in schema [(condition :key) :type])
                   options (key-type->options key-type)]
               [:select {:value (condition :option)
                         :on-change (fn [e]
                                      (dispatch! [:sculpture.advanced-search/update-condition index :option (keyword (.. e -target -value))])
                                      (dispatch! [:sculpture.advanced-search/update-condition index :value nil]))}
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
                                           (dispatch! [:sculpture.advanced-search/update-condition index :value v]))})]))]

          [:button {:on-click (fn [_]
                                (dispatch! [:sculpture.advanced-search/remove-condition index]))} "×"]])
       @(subscribe [:sculpture.advanced-search/conditions]))

     [:button {:on-click (fn [_]
                           (dispatch! [:sculpture.advanced-search/add-condition]))}
      "+"]]

    [:button
     {:on-click (fn [_] (dispatch! [:sculpture.advanced-search/search]))}
     "Search"]

    (let [results @(subscribe [:sculpture.advanced-search/results])]
      [:div.results
       [:div (count results) " Results"]
       [:div.entity-list
        (when (seq results)
          (for [entity results]
            ^{:key (entity :id)}
            [entity-row-view entity]))]])]])
