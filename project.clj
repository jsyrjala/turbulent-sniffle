(defproject ruuvi "0.0.1"
    :description "Ruuvi server"
    :main ruuvi.core/main
    :dependencies [
                   [org.clojure/clojure "1.6.0"]
                   [io.aviso/rook "0.1.36"]
                   [ring "1.3.2"]
                   [ring/ring-json "0.4.0"]
                   [com.cognitect/transit-clj "0.8.281"]
                   [metosin/ring-swagger-ui "2.1.2"]
                   ;; misc
                   [ring-cors "0.1.4"]
                   [ch.qos.logback/logback-classic "1.1.3"]
                   ])
