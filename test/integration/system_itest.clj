(ns integration.system-itest
  (:require [midje.sweet :refer :all]
            [ruuvi.system :refer [] :as system]
            )

  )

(defn valid-system? []
  true)

(fact "System can start and stop."
      (let [system (system/create-system "test/data/system-config.edn")]
        (fact "Before starting contains components"
              (keys system) => (contains [:db :http-server :nrepl-server :ring-handler] :gaps-ok :in-any-order)
              )

        (system/start-system system)
        (fact "Started system contains components"
              (keys system) => (contains [:db :http-server :nrepl-server :ring-handler] :gaps-ok :in-any-order)
              )

        (system/stop-system system)
        (fact "Stopped system contains components"
              (keys system) => (contains [:db :http-server :nrepl-server :ring-handler] :gaps-ok :in-any-order)
              )

        (system/start-system system)
        (fact "System can start after stopping"
              (keys system) => (contains [:db :http-server :nrepl-server :ring-handler] :gaps-ok :in-any-order)
              )

        (system/stop-system system)
        (fact "Re started system can stop"
              (keys system) => (contains [:db :http-server :nrepl-server :ring-handler] :gaps-ok :in-any-order)
              )

        )
      )
