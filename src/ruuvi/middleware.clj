(ns ruuvi.middleware
  "Misc middlewares"
  (:require [clojure.string :as string]
            [clojure.tools.logging :refer [error debug info]]))

(defn wrap-x-forwarded-for
  "Replace value of remote-addr -header with value of X-Forwarded-for -header if available."
  [handler]
  (fn [request]
    (if-let [xff (get-in request [:headers "x-forwarded-for"])]
      (handler (assoc request :remote-addr (last (string/split xff #"\s*,\s*"))))
      (handler request))))

(defn wrap-request-logger
  "Logs each incoming request"
  [handler request-counter]
  (fn [request]
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
