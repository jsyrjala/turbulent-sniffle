(ns ruuvi.system
  (:require [com.stuartsierra.component :refer [using start stop] :as component]
            [ruuvi.database.connection]
            [ruuvi.database.migration]
            [ruuvi.services.nrepl]
            [ruuvi.services.http-server]
            [ruuvi.config :as config]
            )
  )

(defn create-system [config-file]
  (let [system-config (config/read-config config-file)
        {:keys [database nrepl http-server]} system-config
        database (-> system-config :database)
        {:keys [db-spec migration]} database
        ]
    (component/system-map
     :db (ruuvi.database.connection/new-db-pool db-spec)
     :nrepl-server (ruuvi.services.nrepl/new-nrepl-server nrepl)
     :migration (using (ruuvi.database.migration/new-database-migration migration)
                       [:db])
     :http-server (ruuvi.services.http-server/new-http-server http-server nil)
     )))

(defn start-system [system]
  (component/start system))

(defn stop-system [system]
  (component/stop system))
