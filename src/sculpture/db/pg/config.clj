(ns sculpture.db.pg.config
  (:require
    [environ.core :refer [env]]))

(def ^:dynamic db-spec (env :database-url))
