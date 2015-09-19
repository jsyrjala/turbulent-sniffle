(ns ruuvi.resources.meta
  "Data about service"
  {:swagger-tag "Meta"}
  (:require
   [ruuvi.resources.domain :as domain]
   [clojure.tools.logging :as log :refer [info debug]]
   [ring.util.http-response :as r]
   )
  (:import
   [javax.servlet.http HttpServletResponse]
   )
  )


;; TODO move to some util
(defn ^:private always-ok-response [schema]
  {HttpServletResponse/SC_OK schema})

(defn ping
  "Ping Pong"
  {:route [:get ["ping"]]
   :responses (always-ok-response [domain/Pong])}
  []
  (r/ok {:server "RuuviServer"
         :version "0.0.1"
         :server_time (java.util.Date.)})
  )
