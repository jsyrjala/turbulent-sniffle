(ns ruuvi.core
  "start of everything"
  (:import
   [org.eclipse.jetty.server Server])
  (:require
   [ruuvi.swagger :as swagger]
   [ruuvi.middleware :as middleware]
   [ruuvi.security :as sec]
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
   [ring.middleware.resource :refer [wrap-resource]]
   [buddy.auth.middleware :refer [wrap-authentication]])
  )

(def auth-conf {:passphrase "dummy"
                :private-key "test/auth_privkey.pem"
                :public-key "test/auth_pubkey.pem"})

(def wrap-rook-middlewares (rook/compose-middleware
                            sv/wrap-with-schema-validation
                            ))

(def ^{:private true} request-counter (atom 0))

(defn- app []
  (-> (rook/namespace-handler
       {:context ["api"]
        :swagger-options swagger/swagger-options
        :default-middleware sv/wrap-with-schema-validation}
       ["events" 'ruuvi.resources.events]
       ["auth" 'ruuvi.resources.auth])
      (wrap-authentication (sec/auth-backend auth-conf))
      rook/wrap-with-standard-middleware
      (rook/wrap-with-injections {:auth-conf auth-conf})
      (swagger/wrap-swagger-ui "/doc" "/api/swagger.json")
      (middleware/wrap-request-logger request-counter)
      middleware/wrap-x-forwarded-for
      (cors/wrap-cors
       :access-control-allow-origin #".*"
       :access-control-allow-methods [:get :put :post :delete]
       :access-control-allow-headers ["Content-Type"])
      )
  )

(defn start-server
  "Starts a server on the named port, and returns a function that shuts it back down."
  [port config]
  (let [opts {:reload true
              :log false
              :track true
              :standard true
              :exceptions true}
        ^Server server (jetty/run-jetty (server/construct-handler opts #'app)
                                        {:port port :join? false})]
    (log/infof "Listening on port %d." port)
    #(.stop server)))

