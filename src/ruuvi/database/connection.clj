(ns ruuvi.database.connection
  "Database connection pools"
  (:require [clojure.tools.logging :refer [debug error info] :as log]
            [com.stuartsierra.component :as component]
            )
  (:import [com.zaxxer.hikari HikariDataSource HikariConfig]))

(defn- pool-name [random-pool-name]
  (if random-pool-name
    (str "ruuvi-db-hikari-" (rand-int 999999))
    "ruuvi-db-hikari"))

(defn- make-hikari-pool [db-spec]
  (let [hikari-config (HikariConfig.)
        {:keys [datasource-classname
                connection-uri
                username
                password
                max-connections
                connection-test-query
                random-pool-name]} db-spec]
    (doto hikari-config
      (.setRegisterMbeans true)
      (.setDataSourceClassName datasource-classname)
      (.setMaximumPoolSize max-connections)
      (.setConnectionTestQuery connection-test-query)
      (.addDataSourceProperty "URL", connection-uri)
      (.addDataSourceProperty "user" username)
      (.addDataSourceProperty "password" password)
      (.setPoolName (pool-name random-pool-name)))
    (HikariDataSource. hikari-config)
  ))


(defn- make-db-spec [db-spec]
  {:datasource (make-hikari-pool db-spec)}
  )

(defrecord HikariConnectionPool
  [db-spec]
  component/Lifecycle
  (start [component]
         (debug "HikariConnectionPool starting")
         (assoc component :datasource (make-hikari-pool db-spec))
         )
  (stop [component]
        (debug "HikariConnectionPool stopping")
        (when-let [datasource (-> component :datasource)]
          (.close datasource))
        (dissoc component :datasource)
        )
  )

(defn new-db-pool
  "Create new database connection pool component. You can use the
  component also as a db-spec value"
  [db-spec]
  (map->HikariConnectionPool {:db-spec db-spec}))
