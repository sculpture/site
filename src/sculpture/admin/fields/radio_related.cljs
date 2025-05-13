(ns sculpture.admin.fields.radio-related
  (:require
    [reagent.core :as r]
    [humandb.ui.fields.core :refer [field]]))

(defmethod field :radio-related
  [{:keys [value on-change options-fn entity] :as all}]
  (r/with-let [ ;; entity changes, so we keep track of it
               e (r/atom entity)
               options (r/atom [])
               search! (fn []
                         (options-fn @e
                                     (fn [values]
                                       (reset! options values))))
               _ (search!)]
    [:div {:ref (fn []
                  (when (not= entity @e)
                    (reset! e entity)
                    (search!)))}
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
