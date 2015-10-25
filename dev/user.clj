(ns user
  (:require [ruuvi.system]
            [clojure.edn :as edn]
            [clojure.tools.logging :refer [info error] :as log]
            [com.stuartsierra.component :as component]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]]
            [midje.repl :as midje]
            [ruuvi.database.migration :as migration]
            ))

(def system nil)

(defn init
  "Constructs the current development system."
  [& [config-file]]
  (alter-var-root
   #'system
   (constantly (ruuvi.system/create-system (or config-file "dev/config.edn") ))))

(defn start
  "Starts the current development system."
  []
  (try
    (alter-var-root #'system component/start)
    (catch Exception e (error e "Failed to start system")
      (throw e))))

(defn stop
  "Shuts down and destroys the current development system."
  []
  (try
    (alter-var-root #'system
                    (fn [s] (when s (component/stop s))))
    (catch Exception e (error e "Failed to stop system")
      (throw e))))

(defn go
  "Initializes the current development system and starts it running."
  []
  (init)
  (start))

(defn reset
  "Stop any running components, reload all files and restart components."
  []
  (stop)
  (info "Resetting...")
  (refresh :after 'user/go)
  (info "Reset complete")
  :reset-complete)

(defn start-autotest
  "Start auto testing for test/ruuvi. When a file is changed, dependant midje tests are executed."
  []
  (midje/autotest :dirs "test/ruuvi" "src/ruuvi")
  )

(defn database-reset
  ""
  []
  (println (-> system :migration migration/migrate))
  )
