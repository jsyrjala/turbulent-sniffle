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

(defn try-times*
  "Executes thunk. If an exception is thrown, will sleep max sleep-msecs and retry. At most n retries
  are done. If still some exception is thrown it is bubbled upwards in
  the call chain."
  [n sleep-msec thunk]
  (loop [n n]
    (if-let [result (try
                      [(thunk)]
                      (catch Exception e
                        ;;(error "Failed" (.getMessage e))
                        (if (zero? n)
                          (throw e)
                          (Thread/sleep (rand-int sleep-msec))
                          )))]
      (result 0)
      (recur (dec n)))))

(defmacro try-times
  "Executes body. If an exception is thrown, will sleep for a while and then retry. At most n retries
  are done. If still some exception is thrown it is bubbled upwards in
  the call chain."
  [n sleepMsec & body]
  `(try-times* ~n ~sleepMsec (fn [] ~@body)))