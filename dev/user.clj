(ns user
  (:require [ruuvi.system :as system]
            [clojure.edn :as edn]
            [clojure.tools.logging :refer [info error] :as log]
            [com.stuartsierra.component :as component]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]]
            ))

(def system nil)

(defn init
  "Constructs the current development system."
  []
  (alter-var-root
   #'system
   (constantly (system/create-system "dev/config.edn"))))

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

(defn reset []
  (stop)
  (info "Resetting...")
  (refresh :after 'user/go)
  (info "Reset complete")
  :reset-complete)

