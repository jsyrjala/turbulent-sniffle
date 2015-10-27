(ns ruuvi.services.aleph-http-server
  (:require
   [aleph.http :as http]
   [aleph.netty :as netty]
   [com.stuartsierra.component :as component]
   [clojure.tools.logging :refer [debug info]]
   )
  )

(defn- get-running-port [server]
  (netty/port server))

(defrecord AlephServer [config ring-handler]
  component/Lifecycle
  (start [component]
         (let [port (-> config :port)]
           (if (= port 0)
             (info "AlephServer starting in a random free port")
             (info "AlephServer starting in port" (-> config :port)))

           (let [app (-> ring-handler :app)
                 server (http/start-server app config)
                 running-port (get-running-port server)]

               (info "AlephServer started in port" running-port)

             (assoc component :server server
               :server-port running-port))))

  (stop [component]
        (info "AlephServer stopping")

        (when-let [server (-> component :server)]
          (.close server))
        (dissoc component :server :server-port)))

(defn new-http-server [http-server]
  (map->AlephServer {:config http-server})
  )
