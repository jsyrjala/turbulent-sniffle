(ns ruuvi.main
  (:require [clojure.tools.cli :refer [parse-opts]]
            [ruuvi.core :as core])
  (:gen-class)
  )

(defn- file-exists [path]
  (let [file (clojure.java.io/as-file path)]
    (and (.exists file) (.isFile file) (.canRead file))))

(def cli-options
  [["-c" "--config FILE" "Configuration file required"
    :validate [file-exists "File must exist and be readable."]]
   ["-p" "--port PORT" "Port number for server"
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-h" "--help" "Display help"]
   ])

(defn- help-title[]
  (println "ruuvi server")
  )
(defn- cli-help
  ([opts] (cli-help opts 0))
  ([opts exitcode]
  (help-title)
  (println "USAGE:")
  (println (:summary opts))
  (System/exit exitcode)
  ))

(defn- cli-errors [opts]
  (help-title)
  (println "Failed to parse command-line arguments:")
  (doall
   (for [err (-> opts :errors)]
     (println err)
     ))
  (println "Use option --help for more help")
  (System/exit 1)
  )

(defn- parse-cli [args]
  (let [{:keys [options] :as opts} (parse-opts args cli-options)]
    (cond (-> opts :options :help) (cli-help opts)
          (-> opts :errors) (cli-errors opts)
          :default (-> opts :options))))


(defn- stop-system
  "Stop the system."
  []
  (println "TODO stopping system")
  )

(defn- add-shutdown-hook
  "Stop system gracefully when application terminates."
  []
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. stop-system)))

(defn -main [& args]
  (let [opts (parse-cli args)]
    (require '[ruuvi.core])
    (let [start-server (eval `ruuvi.core/start-server)]
      (println "Start with command line" args)
      (add-shutdown-hook)
      (core/start-server (:port opts) (:config opts) )
      )
    ))

;;
;;(-main "foo" "-p" "7000" "-c" "/etc/passwd")
