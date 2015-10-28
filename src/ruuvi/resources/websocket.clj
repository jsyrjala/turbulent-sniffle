(ns ruuvi.resources.websocket
  "Data about service"
  {:swagger-tag "WebSocket"}
  (:require
    [ruuvi.resources.domain :as domain]
    [clojure.tools.logging :as log :refer [info debug]]
    [ring.util.http-response :as r]
    [ring.util.http-status :as status]
    [aleph.http :as http]
    [manifold.bus :as bus]
    [manifold.stream :as s]
    [manifold.deferred :as d]
    )
  )


(def non-websocket-request
  {:status 400
   :headers {"content-type" "application/json"}
   :body "Expected a websocket request."})


(def chatrooms (bus/event-bus))
(defn- chat-handler
  [req]
  (d/let-flow [conn (d/catch
                      (http/websocket-connection req)
                      (fn [_] nil))]
              (if-not conn
                ;; if it wasn't a valid websocket handshake, return an error
                non-websocket-request
                ;; otherwise, take the first two messages, which give us the chatroom and name
                (d/let-flow [_  (s/put! conn "Hello this is chat!")
                             room (s/take! conn)
                             _ (s/put! conn (str "server: your room is " room))
                             name (s/take! conn)
                             _ (s/put! conn (str "server: your name is " name))
                             ]
                            ;; take all messages from the chatroom, and feed them to the client
                            (s/connect
                              (bus/subscribe chatrooms room)
                              conn)
                            ;; take all messages from the client, prepend the name, and publish it to the room
                            (s/consume
                              #(bus/publish! chatrooms room %)
                              (->> conn
                                   (s/map #(str name ": " %))
                                   (s/buffer 100)))))))

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
      (chat-handler req)
      (handler req))))
