(ns sculpture.admin.fields.flexdate
  (:require
    [reagent.core :as r]
    [sculpture.flexdate :as flexdate]
    [humandb.ui.fields.core :refer [field]]))

(defmethod field :flexdate
  [{:keys [value on-change]}]
  [:div
   [:input {:type "text"
            :value value
            :on-change (fn [e]
                         (on-change (.. e -target -value)))}]
   [:span.precision
    (when-let [result (flexdate/parse value)]
      (:flexdate/precision result))]])
