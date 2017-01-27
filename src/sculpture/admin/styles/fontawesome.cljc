(ns sculpture.admin.styles.fontawesome)

(defn fontawesome [unicode]
  {:content (str "\"" unicode "\"")
   :font-family "FontAwesome"})
