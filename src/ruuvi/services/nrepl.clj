(ns ruuvi.services.nrepl
  "nRepl server for remote access"
  (:require
   [clojure.tools.nrepl.server :as nrepl]
   [com.stuartsierra.component :as component]
   [clojure.tools.logging :refer [debug info]]
   ))


(defrecord NReplServer [port]
  component/Lifecycle
  (start [component]
         (debug "NReplServer starting in port" port)
         (let [server (nrepl/start-server :port port)]
           (assoc component :server server)))
  (stop [component]
        (debug "NReplServer stopping")
        (when-let [server (-> component :server)]
          (nrepl/stop-server server))
        (dissoc component :server)))

;; TODO move to util
(defrecord NullComponent []
  component/Lifecycle
  (start [component]
         component)
  (stop [component]
        component))

(defn new-nrepl-server [nrepl-config]
  (if (:enabled nrepl-config)
    (map->NReplServer nrepl-config)
    (->NullComponent)))



