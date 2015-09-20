(ns ruuvi.middleware
  "Misc middlewares"
  (:require [clojure.string :as string]
            [clojure.tools.logging :refer [debug info error] :as log]
            [ring.util.http-response :as r]
            [ring.middleware.json :refer [wrap-json-response]]
            [io.aviso.tracker :as tracker]
            [cheshire.core :as json]))

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
          request-method (:request-method request)
          uri (:uri request)
          query-params (:query-params request)
          start (System/currentTimeMillis)
          remote-addr (:remote-addr request)
          query (if (not (empty? query-params))
                  (str ":query-params "  query-params)
                  "") ]
      (info (str "REQUEST:" counter)
            remote-addr request-method uri query)
      (let [response (handler request)
            duration (- (System/currentTimeMillis) start)
            status (:status response)]
        (info (str "RESPONSE:" counter)
              remote-addr
              status
              duration "msec")
        response) )))

(defn wrap-exception-response
  "Catch exception and turn it to 500 Internal server exception."
  [handler]
  (fn wrap-exception-response-handler [req]
    (try
      (tracker/checkpoint "entry"
        (handler req))
      (catch Throwable ex
        (->
          {:error "Internal server error"
           :description "Something bad happened in the server. It is our fault, not yours. Try again later."
           :server-time (java.util.Date.)}
          ;; wrap-exception-response is the last one to process exceptions
          ;; so it needs to return a string
          (json/generate-string {:pretty true})
          r/internal-server-error
          )))))
