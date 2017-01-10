(ns sculpture.admin.views.styles
  (:require
    [sculpture.admin.styles.app :refer [styles]]))

(defn styles-view []
  [:style
   {:type "text/css"
    :dangerouslySetInnerHTML
    {:__html styles}}])
