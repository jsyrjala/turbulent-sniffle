(ns ruuvi.config
  "Reading configuration files"
  (:require
   [clojure.edn :as edn]
   ))

(defn- validate
  "Validate configuration file. Check that all references files are found, all required data is found etc."
  [config]
  ;; TODO implement, use Schema to validate structure...
  config)

(defn read-config
  "Read and validates configuration file."
  [file]
  (with-open [r (java.io.PushbackReader.
                 (clojure.java.io/reader file))]
    (validate (doall (edn/read r)))))
