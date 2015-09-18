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

(defn- enable-jmx [server]
  (let [mbean-container (MBeanContainer. (ManagementFactory/getPlatformMBeanServer))]
    (doto server
      (.addEventListener mbean-container);
      (.addBean mbean-container)
      (.addBean (Log/getLog)))
      mbean-container))

(defn- disable-jmx [server mbean-container]
  (doto server
    (.removeEventListener mbean-container)
    (.removeBean mbean-container)
    (.removeBean (Log/getLog))))

(defn- get-running-port [server]
  (.getLocalPort (aget (.getConnectors server) 0)))

(defrecord JettyServer [config ring-handler]
  component/Lifecycle
  (start [component]
         (let [port (-> config :port)]
           (if (= port 0)
             (info "JettyServer starting in a random free port")
             (info "JettyServer starting in port" (-> config :port)))

           (let [app (-> ring-handler :app)
                 server (jetty/run-jetty app
                                         (assoc config
                                           :join? false))
                 running-port (get-running-port server)]

               (info "JettyServer started in port" running-port)

             (assoc component :server server
               :server-port running-port
               :mbean-container (enable-jmx server)))))

  (stop [component]
        (info "JettyServer stopping")

        (when-let [server (-> component :server)]
          (disable-jmx server (-> component :mbean-container))
          (.stop server))
        (dissoc component :server :server-port :mbean-container)))

(defn new-http-server [http-server]
  (map->JettyServer {:config http-server})
  )
