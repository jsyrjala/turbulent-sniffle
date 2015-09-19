(ns ruuvi.test-util
  (:require
   [clj-http.client :as client]
               [clojure.tools.logging :as log :refer [info debug]]

   [ruuvi.system :refer [] :as system]
   ))

(def system nil)

(defn get-url [path]
  (let [http-server (-> system :http-server)
        port (-> http-server :server-port)
        url (str "http://localhost:" port "/api" path)]
    (:body (client/get url {:accept :json
                            :as :json}))))

(defn init-system
  [config-file]
  (alter-var-root
   #'system
   (constantly (ruuvi.system/create-system config-file))))

(defn start-system
  []
  (alter-var-root #'system system/start-system))

(defn stop-system
  []
  (alter-var-root #'system
                  (fn [s] (when s (system/stop-system s)))))

