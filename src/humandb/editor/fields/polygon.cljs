(ns humandb.editor.fields.polygon
  (:require
    [reagent.core :as r]
    [humandb.editor.fields.core :refer [field]]))

(defmethod field :polygon
  [_]
  [:div
   [:div {:style {:width "200px"
                  :height "200px"
                  :background "gray"}}
    "MAP"]]
  )
