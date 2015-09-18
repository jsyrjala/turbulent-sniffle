(ns ruuvi.util
  (:require [clojure.string :as string])
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
