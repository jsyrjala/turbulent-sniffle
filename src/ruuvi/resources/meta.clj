(ns ruuvi.resources.meta
  "Data about service"
  {:swagger-tag "Meta"}
  (:require
   [ruuvi.resources.domain :as domain]
   [clojure.tools.logging :as log :refer [info debug]]
   [ring.util.http-response :as r]
   [ring.util.http-status :as status]
   )
  )

(defn ping
  "Ping Pong"
  {:route [:get ["ping"]]
   :responses {status/ok domain/Pong}}
  []
  (r/ok {:server "RuuviServer"
         :version "0.0.1"
         :server_time (java.util.Date.)})
  )

