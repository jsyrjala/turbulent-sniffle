(ns ruuvi.services.nrepl
  "nRepl server for remote access"
  (:require
   [clojure.tools.nrepl.server :as nrepl]
   [com.stuartsierra.component :as component]
   [clojure.tools.logging :refer [debug info]]
   ))


(defn- get-running-port [server]
  (.getLocalPort (-> server :server-socket)))

(defrecord NReplServer [port]
  component/Lifecycle
  (start [component]
         (if (= port 0)
           (debug "NReplServer starting in random port")
           (debug "NReplServer starting in port" port))
         (let [server (nrepl/start-server :port port)
               running-port (get-running-port server)]
           (debug "nREPL server running in port" port)
           ;; TODO Running port is zero here because nREPL binds the port asynchronously
           (assoc component :server server :server-port running-port)))
  (stop [component]
        (debug "NReplServer stopping")
        (when-let [server (-> component :server)]
          (nrepl/stop-server server))
        (dissoc component :server :server-port)))

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



