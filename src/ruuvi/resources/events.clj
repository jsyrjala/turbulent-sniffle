(ns ruuvi.resources.events
  "Desc for namespace"
  {:swagger-tag "Events"
   :swagger-summary "Event related operations"}
  (:require [ring.util.response :as r]
            [clojure.tools.logging :as log :refer [info debug]]
            [ruuvi.resources.domain :as domain]
            [io.aviso.rook.swagger :as sw])

  (:import [javax.servlet.http HttpServletResponse]))

(defn create
  "Eloquent documentation for create"
  {:summary "Create a new event"
   :description "API(swagger docs) overrides docstring"
   :body-schema domain/NewEvent}
  [^:request-key body-params]
  (r/response {:hello "world"})
  )
