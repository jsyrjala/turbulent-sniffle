(ns ruuvi.services.http-server
  (:require
   [ring.adapter.jetty :as jetty]
   [com.stuartsierra.component :as component]
   [clojure.tools.logging :refer [debug info]]
   ))

(defrecord JettyServer [config handler]
  component/Lifecycle
  (start [component]
         (info "JettyServer starting in port" (-> config :port))
         (let [server (jetty/run-jetty handler
                                       (assoc config
                                         :join? false))]
           (assoc component :server server)))
  (stop [component]
        (info "JettyServer stopping")
        (when-let [server (-> component :server)]
          (.stop server))
        (dissoc component :server)))

(defn new-http-server [http-server handler]
  (->JettyServer http-server handler)
  )
