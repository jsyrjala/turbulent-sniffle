(ns ruuvi.system
  (:require [com.stuartsierra.component :refer [using start stop] :as component]
            [ruuvi.database.connection :as connection]
            [ruuvi.database.migration :as migration]

            )
  )


(defn system [config-file]
  (let [config (ruuvi.config/read-config config-file)
        database (-> config :database)
        {:keys [db-spec migration]} database
        ]
    (component/system-map
     :db (connection/new-db-pool db-spec)
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
