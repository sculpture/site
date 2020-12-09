(ns sculpture.admin.fields.enumlookup
  (:require
    [reagent.core :as r]
    [humandb.ui.fields.core :refer [field]]))

(defmethod field :enum-lookup
  [{:keys [value options on-change]}]
  [:div.field.enum-lookup
   (for [[k v] options]
     ^{:key k}
     [:label
      [:input {:type "radio"
               :checked (= value k)
               :on-change (fn []
                            (on-change k))}]
      v])])

