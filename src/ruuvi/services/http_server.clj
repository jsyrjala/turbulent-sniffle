(ns ruuvi.services.http-server
  (:require
   [ring.adapter.jetty :as jetty]
   [com.stuartsierra.component :as component]
   [clojure.tools.logging :refer [debug info]]
   )
  (:import [org.eclipse.jetty.jmx MBeanContainer]
           [java.lang.management ManagementFactory]
           [org.eclipse.jetty.util.log Log]
           )
  )

(defn- enable-jmx[server]
  (let [mb-container (MBeanContainer. (ManagementFactory/getPlatformMBeanServer))]
    (doto server
      (.addEventListener mb-container);
      (.addBean mb-container)
      (.addBean (Log/getLog))))
  )

(defrecord JettyServer [config handler]
  component/Lifecycle
  (start [component]
         (info "JettyServer starting in port" (-> config :port))
         (let [server (jetty/run-jetty handler
                                       (assoc config
                                         :join? false))]
           (enable-jmx server)
           (assoc component :server server)))

  (stop [component]
        (info "JettyServer stopping")
        (when-let [server (-> component :server)]
          (.stop server))
        (dissoc component :server)))

(defn new-http-server [http-server handler]
  (->JettyServer http-server handler)
  )
