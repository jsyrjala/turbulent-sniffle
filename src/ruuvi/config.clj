(ns ruuvi.config
  (:require
   [clojure.edn :as edn]
   )
  )

(defn- validate [config]
  config
  )

(defn read-config [file]
  (with-open [r (java.io.PushbackReader.
                 (clojure.java.io/reader file))]
    (validate (doall (edn/read r)))))


;;(read-config "dev/config.edn")


