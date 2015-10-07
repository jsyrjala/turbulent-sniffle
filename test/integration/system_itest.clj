(ns integration.system-itest
  (:require [midje.sweet :refer :all]
            [ruuvi.test-util :as test-util :refer :all]))

(fact "System can start and stop."
      (init-system "test/data/system-config.edn")

      (fact "Before starting contains components"
            (keys test-util/system) => (contains [:db :http-server :nrepl-server :ring-handler]
                                                 :gaps-ok :in-any-order))

      (start-system)
      (fact "Started system contains components"
            (keys test-util/system) => (contains [:db :http-server :nrepl-server :ring-handler]
                                                 :gaps-ok :in-any-order))
      (fact "Started server responds to PING"
            (:body (get-url "/meta/ping")) => (contains {:server "RuuviServer", :version "0.0.1"}))

      (stop-system)
      (fact "Stopped system contains components"
            (keys test-util/system) => (contains [:db :http-server :nrepl-server :ring-handler]
                                                 :gaps-ok :in-any-order))

      (start-system)
      (fact "System can start after stopping"
            (keys test-util/system) => (contains [:db :http-server :nrepl-server :ring-handler]
                                                 :gaps-ok :in-any-order))
      (fact "Re-started server responds to PING"
            (:body (get-url "/meta/ping")) => (contains {:server "RuuviServer", :version "0.0.1"}))

      (stop-system)
      (fact "Re started system can stop"
            (keys test-util/system) => (contains [:db :http-server :nrepl-server :ring-handler]
                                                 :gaps-ok :in-any-order))
      )
