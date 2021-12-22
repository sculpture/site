(ns sculpture.db.pg.config
  (:require
    [hugsql.core :as hugsql]
    [hugsql.adapter.next-jdbc :as next-adapter]
    [next.jdbc.result-set :as rs]
    [hikari-cp.core :as hikari]
    [sculpture.config :refer [config]]))

(defonce datasource
  (delay
    (hugsql/set-adapter! (next-adapter/hugsql-adapter-next-jdbc))
    (hikari/make-datasource {:jdbc-url (str "jdbc:" (config :db-url))})))

(def ^:dynamic db-spec datasource)
