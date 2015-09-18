(ns ruuvi.main
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log :refer [debug info]]
            )
  (:gen-class)
  )

(defn- file-exists [path]
  (let [file (clojure.java.io/as-file path)]
    (and (.exists file) (.isFile file) (.canRead file))))

(def cli-options
  [["-c" "--config FILE" "Configuration file required"
    :validate [file-exists "File must exist and be readable."]]
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

(defn- cli-errors [opts & [msg]]
  (help-title)
  (println (or msg "Failed to parse command-line arguments:"))
  (doall
   (for [err (-> opts :errors)]
     (println err)
     ))
  (println "Use option --help for more help")
  (System/exit 1)
  )

(defn- validate-cli [opts]
  (when-not (-> opts :options :config)
    (cli-errors opts "Missing mandatory parameter --config")
    ))

(defn- parse-cli [args]
  (let [{:keys [options] :as opts} (parse-opts args cli-options)]
    (cond (-> opts :options :help) (cli-help opts)
          (-> opts :errors) (cli-errors opts)
          :default (validate-cli opts))
    (-> opts :options)))


(defn- stop-system
  "Stop the system."
  [system]
  (let [stop-system (eval `ruuvi.system/stop-system)]
    (stop-system system)))

(defn- add-shutdown-hook
  "Stop the system gracefully when application terminates."
  [system]
  (debug "Registering shutdown hook")
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. (fn [] (stop-system system)))))

(defn- print-process-id
  "Print process id (PID) of the current process"
  []
  (require '[ruuvi.util])
  (let [pid (eval `(ruuvi.util/process-id))]
    (info "Process ID:" pid)))

(defn -main
  "Starting point of the program when running from command line."
  [& args]
  (let [opts (parse-cli args)]

    (require '[ruuvi.system])
    (let [create-system (eval `ruuvi.system/create-system)
          start-system (eval `ruuvi.system/start-system)
          stop-system (eval `ruuvi.system/stop-system)]
      (info "Start with command line" args)
      (print-process-id)

      (let [system (create-system (:config opts) )]
        (add-shutdown-hook system)
        (start-system system)
        )
      )
    ))

