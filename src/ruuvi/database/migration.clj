(ns ruuvi.database.migration
  (:require
   [clojure.tools.logging :refer [debug info]]
   [ragtime.jdbc :as jdbc]
   [ragtime.repl :as repl]
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

(def config {:datastore (jdbc/sql-database db-spec)
             :migrations (jdbc/load-resources "migrations")
             :reporter logger-reporter
             })


;;(repl/migrate config)
;;(repl/rollback config 99999)




