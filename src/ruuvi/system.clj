(ns ruuvi.system
  (:require [com.stuartsierra.component :refer [using start stop] :as component]
            [ruuvi.config :as config]
            [ruuvi.database.connection]
            [ruuvi.database.migration]
            [ruuvi.services.nrepl]
            [ruuvi.services.http-server]
            [ruuvi.services.aleph-http-server]
            [ruuvi.ring-handler]
            )
  )

(defn create-system [config-file]
  (let [system-config (config/read-config config-file)
        {:keys [nrepl http-server development]} system-config
        database (-> system-config :database)
        {:keys [db-spec migration]} database]
    (component/system-map
     :db (ruuvi.database.connection/new-db-pool db-spec)
     :nrepl-server (ruuvi.services.nrepl/new-nrepl-server nrepl)
     :migration (using (ruuvi.database.migration/new-database-migration migration)
                       [:db])
     :ring-handler (using (ruuvi.ring-handler/new-ring-handler development)
                          [:db])
     ;; :http-server (using (ruuvi.services.http-server/new-http-server http-server)
     ;;                    [:ring-handler])

     :http-server (using (ruuvi.services.aleph-http-server/new-http-server http-server)
                         [:ring-handler])
     )))

(defn start-system[system]
  (component/start system))

(defn stop-system[system]
  (component/stop system))
