(ns ruuvi.system
  (:require [com.stuartsierra.component :refer [using start stop] :as component]
            [ruuvi.database.connection :as connection]
            [ruuvi.database.migration :as migration]
            [ruuvi.services.nrepl :as nrepl]
            [ruuvi.config :as config]
            )
  )

(defn system [config-file]
  (let [config (config/read-config config-file)
        {:keys [database nrepl]} config
        database (-> config :database)
        {:keys [db-spec migration]} database
        ]
    (component/system-map
     :db (connection/new-db-pool db-spec)
     :nrepl-server (nrepl/new-nrepl-server nrepl)
     :migration (using (migration/new-database-migration migration)
                       [:db])
      )))

;;
(comment
  (def s (start (system "dev/config.edn")))
  (stop s)

  (keys s)

  (migration/migrate (-> s :migration))
  (migration/rollback-all (-> s :migration))
  )
