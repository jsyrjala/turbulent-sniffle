(ns ruuvi.database.migration
  "Database migrations are a way to create and update database structure
  (e.g table and indices). Migration system keeps track what migrations
  are already applied to database.
  "
  (:require
   [clojure.tools.logging :refer [debug info]]
   [ragtime.jdbc :as jdbc]
   [ragtime.repl :as repl]
   [com.stuartsierra.component :as component]
   ))

(def db-spec {:classname   "org.h2.Driver"
              :subprotocol "h2"
              :subname     "mem:test1;DB_CLOSE_DELAY=-1"
              })

(defn- logger-reporter [op id]
  (case op
    :up (info "Applying database migration" id)
    :down (info "Rollbacking database migration" id)
  ))

(defn- create-config [db-spec]
  {:datastore (jdbc/sql-database db-spec)
   :migrations (jdbc/load-resources "migrations")
   :reporter logger-reporter
   })

(defprotocol Migration
  (migrate [component])
  (rollback [component count])
  (rollback-all [component]))

(defrecord DatabaseMigration [db]
  component/Lifecycle
  (start [component]
         (debug "DatabaseMigration starting")
         (assoc component :config (create-config db)))

  (stop [component]
        (debug "DatabaseMigration stopping")
        (dissoc component :config))

  Migration
  (migrate
   [component]
   (repl/migrate (-> component :config)))

  (rollback
   [component count]
   (repl/rollback (-> component :config) count))

  (rollback-all
   [component]
   (rollback component 999999))
  )

(defn new-database-migration [config]
  (map->DatabaseMigration {}))

;;(repl/migrate config)
;;(repl/rollback config 99999)




