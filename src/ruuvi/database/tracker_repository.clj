(ns ruuvi.database.tracker-repository
  "Tracker repository"
  (:require
    [clojure.tools.logging :refer [trace debug info warn]]
    [ruuvi.database.db-util :as db-util]
    [ruuvi.security :as sec]
    [buddy.hashers :as hs]
    [slingshot.slingshot :refer [try+ throw+]]))

(defn create-tracker! [conn user tracker]
  ;; TODO password hash
  (let [tracker (assoc tracker :owner_id (user :id))]
    (try+
      (db-util/insert! conn :trackers tracker)
      (catch java.sql.SQLException e
        (info "Failed to create tracker" (tracker :tracker_code) "assuming due constraints")
        (throw+ {:error :tracker-already-exists
                 :description "Tracker already exists"})
        )
      )))

(defn get-tracker [conn tracker-code]
  (let [tracker (db-util/get-row conn :trackers ["tracker_code = ?" tracker-code])]
    tracker))

(defn- valid-password? [tracker password]
  (and password (= password (-> tracker password)))
  )

(defn- valid-hmac? [tracker event]
  (let [{:keys [shared_secret]} tracker
        request-hmac (event :hmac)
        mac-message (sec/generate-mac-message event :mac)
        computed-hmac (sec/compute-hmac shared_secret mac-message)]
    (= request-hmac computed-hmac)))

(defn authenticate-tracker
  "Authenticate tracker based on data in event (password and hmac)"
  [conn event]
  (let [{:keys [password tracker_code]} (-> event :password)
        tracker (db-util/get-row-insecure conn :trackers ["tracker_code = ?" tracker_code])
       ]
    (cond
      (not tracker) false
      (valid-password? tracker password) true
      (valid-hmac? tracker event) true
      :default false)))
