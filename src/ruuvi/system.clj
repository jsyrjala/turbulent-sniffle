(ns ruuvi.system
  (:require [com.stuartsierra.component :refer [using start stop] :as component]
            [ruuvi.database.connection :as connection]
            [ruuvi.database.migration :as migration]
            [ruuvi.services.nrepl :as nrepl]
            [ruuvi.config :as config]
            )
  )

;; TODO support also direct map as param
(defn create-system [config-file]
  (let [system-config (config/read-config config-file)
        {:keys [database nrepl]} system-config
        database (-> system-config :database)
        {:keys [db-spec migration]} database
        ]
    (component/system-map
     :db (connection/new-db-pool db-spec)
     :nrepl-server (nrepl/new-nrepl-server nrepl)
     :migration (using (migration/new-database-migration migration)
                       [:db])
      )))

(defn start-system [system]
  (component/start system))

(defn stop-system [system]
  (component/stop system))
