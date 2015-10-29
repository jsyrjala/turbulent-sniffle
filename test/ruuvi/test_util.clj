(ns ruuvi.test-util
  (:require
   [clj-http.client :as http]
   [clojure.tools.logging :refer [info debug]]
   [ruuvi.system :as system]
   [clojure.java.jdbc :as jdbc]
   [java-jdbc.sql :as sql]
   [ruuvi.database.migration :as migration]
   [ruuvi.database.user-repository :as users]
  ))

(def system nil)

(defn db [] (-> system :db))

(defn- create-url [path]
  (let [http-server (-> system :http-server)
        port (-> http-server :server-port)]
    (str "http://localhost:" port "/api" path)))

(defn get-url [path]
  (let [url (create-url path)]
    (:body (http/get url {:accept :json
                          :as :json}))))

(defn post-url [path body & headers]
  (let [url (create-url path)
        result (http/post url (merge {:form-params body
                                      :content-type :json
                                      :as :json}
                          headers))]
    ;;(println "post-url" path (:body result))
    (:body result) ))

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


(def users [{:username "jim"
             :email "jim@example.com"
             :password "verysecret"}
            {:username "john"
             :email "john@example.com"
             :password "secretpass"}
            ])

(defn list-table [conn table]
  (jdbc/query conn (sql/select * table [])))

(defn create-users []
  (doseq [user users]
    (users/create-user! (db) user)))

(defn init-system
  [filename & [opts]]
  (create-system filename)
  (start-system)
  (-> system
      :migration
      migration/migrate)
  (-> system
      :db
      (jdbc/execute! ["delete from users"]) )
  (cond (-> opts :users) (create-users)) )
