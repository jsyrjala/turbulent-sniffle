(ns ruuvi.system
  (:require [com.stuartsierra.component :refer [using start stop] :as component]
            [ruuvi.config :as config]
            [ruuvi.database.connection]
            [ruuvi.database.migration]
            [ruuvi.services.nrepl]
            [ruuvi.services.http-server]
            [ruuvi.ring-handler]
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
     :ring-handler (using (ruuvi.ring-handler/new-ring-handler)
                          [:db])
     :http-server (using (ruuvi.services.http-server/new-http-server http-server)
                         [:ring-handler])
     )))

(defn start-system[config-file]
  (component/start (create-system config-file)))

(defn stop-system[system]
  (component/stop system))
