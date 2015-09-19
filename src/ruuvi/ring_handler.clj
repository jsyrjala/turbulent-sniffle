(ns ruuvi.ring-handler
  (:require
   [com.stuartsierra.component :as component]
   [clojure.tools.logging :refer [debug info]]

   [io.aviso.rook :as rook]
   [ring.util.response :as r]
   [io.aviso.rook.server :as server]

   [io.aviso.rook.schema-validation :as sv]
   [io.aviso.rook.response-validation :as rv]
   [io.aviso.rook.swagger :as sw]
   [schema.core :as s]
   [ring.middleware.cors :as cors]
   [ring.middleware.json :refer [wrap-json-body]]
   [ring.middleware.resource :refer [wrap-resource]]
   [buddy.auth.middleware :refer [wrap-authentication]]

   [ruuvi.swagger :as swagger]
   [ruuvi.middleware :as middleware]
   [ruuvi.security :as sec]

   )
  )

(def auth-conf {:passphrase "dummy"
                :private-key "test/auth_privkey.pem"
                :public-key "test/auth_pubkey.pem"})


(def wrap-rook-middlewares (rook/compose-middleware
                            sv/wrap-with-schema-validation
                            ))

(def ^{:private true} request-counter (atom 0))

(defn- create-app []
  (-> (rook/namespace-handler
       {:context ["api"]
        :swagger-options swagger/swagger-options
        :default-middleware sv/wrap-with-schema-validation}
       ["meta" 'ruuvi.resources.meta]
       ["auth" 'ruuvi.resources.auth]
       ["events" 'ruuvi.resources.events]
       )

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

(defrecord RingHandler [db]
  component/Lifecycle
  (start [component]
         (debug "RingHandler starting")
         (let [app (create-app)
               wrapped-app (rook/wrap-with-injections app component)]
           (assoc component :app wrapped-app)))
  (stop [component]
        (debug "RingHandler stopping")
        (dissoc component :app)))

(defn new-ring-handler []
  (map->RingHandler {}))
