(ns ruuvi.database.connection
  (:require [clojure.tools.logging :refer [debug error info] :as log]
            )
  (:import [com.zaxxer.hikari HikariDataSource HikariConfig]))

(defn- make-hikari-pool [db-spec]
  (let [config (HikariConfig.)
        {:keys [datasource-classname
                connection-uri
                username
                password
                max-connections
                connection-test-query]} db-spec]
    (doto config
      (.setDataSourceClassName datasource-classname)
      (.setMaximumPoolSize max-connections)
      (.setConnectionTestQuery connection-test-query)
      (.addDataSourceProperty "URL",  connection-uri)
      (.addDataSourceProperty "user" username)
      (.addDataSourceProperty "password" password)
      (.setPoolName "ruuvi-db-hikari"))
    (HikariDataSource. config)
  ))



