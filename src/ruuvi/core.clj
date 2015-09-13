(ns ruuvi.core
  "start of everything"
  (:import
   [org.eclipse.jetty.server Server])
  (:require
   [ruuvi.swagger :as swagger]
   [ruuvi.middleware :as middleware]
   [ring.adapter.jetty :as jetty]
   [io.aviso.rook :as rook]
   [ring.util.response :as r]
   [clojure.tools.logging :as log :refer [info]]
   [io.aviso.rook.server :as server]
   [io.aviso.rook.schema-validation :as sv]
   [io.aviso.rook.response-validation :as rv]
   [io.aviso.rook.swagger :as sw]
   [schema.core :as s]
   [ring.middleware.cors :as cors]
   [ring.middleware.json :refer [wrap-json-body]]
   [ring.middleware.resource :refer [wrap-resource]])
  )

(def dbconn "postgres")
(def system "goo-system")
(def wrap-rook-middlewares (rook/compose-middleware
                            sv/wrap-with-schema-validation
                            ))

(def ^{:private true} request-counter (atom 0))
(defn- app []
  (-> (rook/namespace-handler
       {:context ["api"]
        :swagger-options swagger/swagger-options
        :default-middleware sv/wrap-with-schema-validation}
       ["events" 'ruuvi.resources.events])
      rook/wrap-with-standard-middleware
      (rook/wrap-with-injections {:dbconn dbconn :system system})
      (swagger/wrap-swagger-ui "/doc" "/api/swagger.json")
      (cors/wrap-cors
       :access-control-allow-origin #".*"
       :access-control-allow-methods [:get :put :post :delete]
       :access-control-allow-headers ["Content-Type"])
      (middleware/wrap-request-logger request-counter)
      middleware/wrap-x-forwarded-for
      )
  )

(defn start-server
  "Starts a server on the named port, and returns a function that shuts it back down."
  [port]
  (let [opts {:reload true
              :log false
              :track true
              :standard true
              :exceptions true}
        ^Server server (jetty/run-jetty (server/construct-handler opts #'app)
                                        {:port port :join? false})]
    (log/infof "Listening on port %d." port)
    #(.stop server)))

(defn main
  []
  (start-server 8080))
