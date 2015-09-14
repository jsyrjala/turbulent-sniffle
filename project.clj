(defproject ruuvi "0.0.1"
  :description "Ruuvi server"
  :main ruuvi.core/main
  :dependencies
  [
   [org.clojure/clojure "1.6.0"]
   [io.aviso/rook "0.1.36"]
   [ring "1.4.0"]
   [ring/ring-json "0.4.0"]
   [com.cognitect/transit-clj "0.8.281"]
   [metosin/ring-swagger-ui "2.1.2"]
   ;; misc
   [ring-cors "0.1.7"]
   [ch.qos.logback/logback-classic "1.1.3"]
   [metosin/ring-http-response "0.6.5"]
   [clj-time "0.11.0"]
   ;; security
   [buddy/buddy-auth "0.6.2"]
   [buddy/buddy-sign "0.6.0"]

   [buddy/buddy-hashers "0.6.0"]

   ;; database
   [org.clojure/java.jdbc "0.4.1"]
   [java-jdbc/dsl "0.1.3"]
   [honeysql "0.6.1"]
   [yesql "0.4.2"]
   [org.postgresql/postgresql "9.3-1100-jdbc41"]
   [com.zaxxer/HikariCP "2.3.9"]
   [ragtime "0.5.2"]
   ;; database test
   [com.h2database/h2 "1.4.189"]
   ])
