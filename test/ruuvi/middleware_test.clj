(ns ruuvi.middleware-test
  (:require
    [ruuvi.middleware :refer :all]
    [midje.sweet :refer :all]
    [slingshot.slingshot :refer [throw+]]
    [clj-uuid :as uuid]))

(defn- handler [req]
  {:body "data"
   :status 200})

(defn- throw-handler [req]
  (throw+ "intentional exception for unit-tests" ))

(fact "wrap-exception-response converts exception to 500 Internal server error"
      (let [response ((wrap-exception-response throw-handler) {:request-id "dummy"})]
        response => (contains {:status 500})
        (response :body) => (contains "\"error\" : \"Internal server error\"" )
        (response :body) => (contains "\"description\" : \"Something bad happened in the server. It is our fault, not yours. Try again later.\"" )
        (response :body) => (contains "\"request_id\" : \"dummy\"" )
        ))

(fact "wrap-exception-response passes response as is when no exception occurs"
      ((wrap-exception-response handler)
        {:request-id "dummy"}) => {:status 200 :body "data"})

(fact "wrap-request-id adds request id to request map"
      (let [response ((wrap-request-id identity) {})]
        (response :request-id) => uuid/uuid-string?))
