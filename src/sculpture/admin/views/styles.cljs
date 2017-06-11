(ns sculpture.admin.views.styles
  (:require
    [sculpture.admin.styles.core :refer [styles]]))

(defn styles-view []
  [:style
   {:type "text/css"
    :dangerouslySetInnerHTML
    {:__html styles}}])
