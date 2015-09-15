(ns ruuvi.config
  (:require
   [clojure.edn :as edn]
   )
  )


(defn read-config [file]
  (with-open [r (java.io.PushbackReader.
                 (clojure.java.io/reader file))]
    (doall (edn/read r))))


;;(read-config "dev/config.edn")


