(defproject ruuvi "0.0.1"
  :description "Ruuvi server"
  :main ruuvi.main
  :plugins [[lein-environ "1.0.1"]
            [lein-midje "3.1.3"]
            ;;[lein-marginalia "0.8.0"]
            [michaelblume/lein-marginalia "0.9.0"]]

  :dependencies
  [
   [org.clojure/clojure "1.7.0"]
   [io.aviso/rook "0.1.39"]
   [ring "1.4.0"]
   [ring/ring-json "0.4.0"]
   [com.cognitect/transit-clj "0.8.285"]
   [metosin/ring-swagger-ui "2.1.3-2"]
   ;; http
   [ring/ring-jetty-adapter "1.4.0"]
   [org.eclipse.jetty/jetty-server "9.3.6.v20151106"]
   [org.eclipse.jetty/jetty-jmx "9.3.6.v20151106"]
   [aleph "0.4.1-beta3"]
   [potemkin "0.4.1"]

   ;; misc
   [ring-cors "0.1.7"]
   [ch.qos.logback/logback-classic "1.1.3"]
   [metosin/ring-http-response "0.6.5"]
   [clj-time "0.11.0"]
   [environ "1.0.1"]
   [com.stuartsierra/component "0.3.0"]
   [org.clojure/tools.cli "0.3.3"]
   [org.clojure/tools.nrepl "0.2.12"]
   [danlentz/clj-uuid "0.1.6"]

   ;; security
   [buddy/buddy-auth "0.8.1"]
   [buddy/buddy-sign "0.8.1"]
   [buddy/buddy-hashers "0.9.0"]

   ;; database
   [org.clojure/java.jdbc "0.4.2"]
   [java-jdbc/dsl "0.1.3"]
   [honeysql "0.6.2"]
   [yesql "0.5.1"]
   [org.postgresql/postgresql "9.3-1100-jdbc41"]
   [com.zaxxer/HikariCP "2.4.2"]
   [ragtime "0.5.2"]
   ;; database test
   [com.h2database/h2 "1.4.190"]
   ]

  :profiles {:dev {:source-paths ["dev"]
                   :resource-paths ["swagger-ui"]
                   :dependencies [[clj-http "2.0.0"]
                                  [ring-mock "0.1.5"]
                                  [lein-light-nrepl "0.2.0"]
                                  [midje "1.8.2"]
                                  [org.clojure/tools.namespace "0.2.11"]
                                  ]}
             :uberjar {:resource-paths ["swagger-ui"]
                       :aot :all}
             }
  )
