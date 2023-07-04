(ns sculpture.admin.fields.radio-related
  (:require
    [reagent.core :as r]
    [sculpture.flexdate :as flexdate]
    [humandb.ui.fields.core :refer [field]]))

(defmethod field :radio-related
  [{:keys [value on-change options-fn entity] :as all}]
  (r/with-let [options (r/atom [])
               _ (options-fn entity
                             (fn [values]
                               (reset! options values)))]
    [:div
     (for [option @options]
       ^{:key (hash (:value option))}
       [:div
        [:label
         [:input {:type "radio"
                  :value (:value option)
                  :checked (= (:value option) value)
                  :on-change (fn [e]
                               (on-change (:value option)))}]
         (:label option)]])]))
