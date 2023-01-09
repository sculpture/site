(ns sculpture.admin.fields.enumlookup
  (:require
    [reagent.core :as r]
    [humandb.ui.fields.core :refer [field]]))

(defmethod field :enum-lookup
  [{:keys [value options on-change]}]
  (r/with-let [options-atom (if (fn? options)
                              (r/atom {})
                              (r/atom options))
               _ (when (fn? options)
                   (options (fn [o]
                              (reset! options-atom o))))]
    [:div.field.enum-lookup
     (for [[k v] @options-atom]
       ^{:key k}
       [:label
        [:input {:type "radio"
                 :checked (= value k)
                 :on-change (fn []
                              (on-change k))}]
        v])]))

