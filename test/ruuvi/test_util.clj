(ns ruuvi.test-util
  (:require
   [clj-http.client :as client]
               [clojure.tools.logging :as log :refer [info debug]]
   [ruuvi.system :as system]
   [clojure.java.jdbc :as jdbc]
   [ruuvi.database.migration :as migration]
   ))

(def system nil)

(defn get-url [path]
  (let [http-server (-> system :http-server)
        port (-> http-server :server-port)
        url (str "http://localhost:" port "/api" path)]
    (:body (client/get url {:accept :json
                            :as :json}))))

(defn create-system
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

(defn init-system
  [filename]
  (create-system filename)
  (start-system)
  (-> system
      :migration
      migration/migrate)
  (-> system
      :db
      (jdbc/execute! ["delete from users"])
      ))
