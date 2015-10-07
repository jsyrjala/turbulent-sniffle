(ns ruuvi.util
  (:require [cheshire.generate :as generate]
            [clj-time.coerce :as coerce])
  (:import [java.lang.management ManagementFactory])
  )

(defn process-id
  "Try to get id of current process. Returns nill in case of failure."
  []
  (try
    ;; returns "12345@hostname"
    (let [name (.getName (ManagementFactory/getRuntimeMXBean))]
      (Integer. (re-find  #"\d+" name )))
    (catch Exception e
      nil )))

(defn- clj-time-encoder
  [data jsonGenerator]
  (.writeString jsonGenerator (coerce/to-string data)))

(defn- add-json-encoders* []
  (generate/add-encoder
    org.joda.time.DateTime
    (fn [data jsonGenerator]
      (clj-time-encoder data jsonGenerator))))

(def ^{:doc "Register JSON encoders for cheshire (e.g. JodaTime"}
add-json-encoders
  (memoize add-json-encoders*))
