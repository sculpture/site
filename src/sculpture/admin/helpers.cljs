(ns sculpture.admin.helpers
  (:require
    [cljs-time.format :as f]
    [cljs-time.coerce :as c]))

(defn format-date [date format-string]
  (if (and date format-string)
    (f/unparse
      (f/formatter format-string)
      (c/from-date date))))
