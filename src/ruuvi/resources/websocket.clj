(ns ruuvi.resources.websocket
  "Data about service"
  {:swagger-tag "WebSocket"}
  (:require
    [ruuvi.resources.domain :as domain]
    [clojure.tools.logging :as log :refer [info debug]]
    [ring.util.http-response :as r]
    [ring.util.http-status :as status]
    [aleph.http :as http]

    [manifold.stream :as s]
    [manifold.deferred :as d]
    )
  )


(def non-websocket-request
  {:status 400
   :headers {"content-type" "application/json"}
   :body "Expected a websocket request."})

(defn- websocket-connection-handler
  "This is another asynchronous handler, but uses `let-flow` instead of `chain` to define the
   handler in a way that at least somewhat resembles the synchronous handler."
  [req]
  (->
    (d/let-flow [socket (http/websocket-connection req)]
                (s/connect socket socket))
    (d/catch
      (fn [_]
        non-websocket-request))))

(defn websocket
  "WebSocket endpoint"
  {:route [:get]
   :description "This is a WebSocket endpoint. You need to use the WebSocket protocol to access this."}
  []
  (throw (IllegalAccessError. "Do not call. Only for swagger documentation.")))

(defn websocket-middleware
  [handler uri]
  (fn [req]
    (if (= (-> req :uri) uri)
      (websocket-connection-handler req)
      (handler req))))
