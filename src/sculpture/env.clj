(ns sculpture.env
  (:require
    [environ.core :as environ]))

(defmacro fetch-from-env [kw]
  (environ/env kw))

