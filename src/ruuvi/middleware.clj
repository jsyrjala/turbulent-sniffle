(ns ruuvi.middleware
  "Misc middlewares"
  (:require [clojure.string :as string]
            [clojure.tools.logging :refer [debug info error] :as log]
            [ring.util.http-response :as r]
            [ring.middleware.json :refer [wrap-json-response]]
            [io.aviso.tracker :as tracker]
            [cheshire.core :as json]
            [ring.util.http-response :as http-response]
            [clj-uuid :as uuid])
  (:import
    [com.fasterxml.jackson.core JsonParseException]))

(defn wrap-x-forwarded-for
  "Replace value of remote-addr -header with value of X-Forwarded-for -header if available."
  [handler]
  (fn wrap-x-forwarded-for-handler [request]
    (if-let [xff (get-in request [:headers "x-forwarded-for"])]
      (handler (assoc request :remote-addr (last (string/split xff #"\s*,\s*"))))
      (handler request))))

(defn wrap-request-logger
  "Logs each incoming request"
  [handler request-counter]
  (fn wrap-request-logger-handler [request]
    (let [counter (swap! request-counter inc)
          request-id (:request-id request)
          request-method (:request-method request)
          uri (:uri request)
          query-params (:query-params request)
          start (System/currentTimeMillis)
          remote-addr (:remote-addr request)
          query (if (not (empty? query-params))
                  (str ":query-params "  query-params)
                  "") ]
      (info (str "REQUEST:" counter)
            request-id
            remote-addr request-method uri query)
      (let [response (handler request)
            duration (- (System/currentTimeMillis) start)
            status (:status response)]
        (info (str "RESPONSE:" counter)
              request-id
              remote-addr
              status
              duration "msec")
        response) )))

(defn wrap-exception-response
  "Catch exception and turn it to 500 Internal server exception."
  [handler]
  (fn wrap-exception-response-handler [{:keys [request-id] :as req}]
    (try
      (tracker/checkpoint
        (handler req))
      (catch Throwable ex
        (->
          {:error "Internal server error"
           :description "Something bad happened in the server. It is our fault, not yours. Try again later."
           :server_time (java.util.Date.)
           :request_id (req :request-id)}
          ;; wrap-exception-response is the last one to process exceptions
          ;; so it needs to return a string
          (json/generate-string {:pretty true})
          r/internal-server-error
          )))))

(defn wrap-request-id
   "Add request-id (UUID) to request map"
   [handler]
   (fn wrap-request-id-handler [request]
     (let [request-id (uuid/to-string (uuid/v1))]
       (tracker/track #(format  "Incoming request %s"  request-id)
         (handler (assoc request :request-id request-id))))
       ))



(defn- bad-format-error-handler [exception _ _]
  (if (instance? JsonParseException exception)
    (http-response/bad-request {:error "Bad request" :description "Malformed data in request"})
    (throw exception)))

(defn wrap-with-standard-middleware
  "The standard middleware that Rook expects to be present before it is passed the Ring request.
  Request parsing will return 400 Bad request if request contains malformed data"
  [handler]
  (-> handler
      (ring.middleware.format/wrap-restful-format :formats [:json-kw :edn]
                                                  :request-error-handler bad-format-error-handler)
      ring.middleware.keyword-params/wrap-keyword-params
      ring.middleware.params/wrap-params))
