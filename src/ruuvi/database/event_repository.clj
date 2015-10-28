(ns ruuvi.database.event-repository
  (:require
    [clojure.tools.logging :refer [trace debug info warn]]
    [ruuvi.database.db-util :as db-util]
    [ruuvi.util :as util]
    [clojure.string :as s]
    [buddy.hashers :as hs]
    [clojure.java.jdbc :as jdbc]
    [clj-time.coerce :as time-conv]
    [slingshot.slingshot :refer [try+ throw+]]))

(defn- create-session! [conn tracker-id session-code timestamp]
  (db-util/insert! conn :event_sessions {:tracker_id        tracker-id
                                         :session_code      session-code
                                         :first_event_time  timestamp
                                         :latest_event_time timestamp}))

(defn get-event-session-for-code [conn tracker-id session-code]
  (db-util/get-row conn
           :event_sessions
           ["tracker_id = ? and session_code = ?"
            tracker-id session-code]))

(defn update-session-activity! [conn session-id timestamp]
  (jdbc/execute! conn
                 ["update event_sessions
                 set latest_event_time = greatest(?, latest_event_time), updated_at = ?
                 where id = ?"
                  (time-conv/to-sql-time timestamp) (db-util/current-sql-timestamp) session-id] ))

(defn get-or-create-session! [conn tracker-id session-code timestamp]
  ;; there may be a burst of events that start a session, than can
  ;; cause duplicate key errors
  (let [existing-session (get-event-session-for-code conn tracker-id session-code)
        session-id (:id existing-session)]
    (if existing-session
      (do
        (update-session-activity! conn session-id timestamp)
        existing-session)
      (create-session! conn tracker-id session-code timestamp)
      )))

(defn- create-event-row! [conn tracker-id session-id event]
  (let [db-event (select-keys event [:event_time
                                     :latitude
                                     :longitude
                                     :horizontal_accuracy
                                     :vertical_accuracy
                                     :speed
                                     :heading
                                     :satellite_count
                                     :altitude])
        db-event (assoc db-event
                   :tracker_id tracker-id
                   :event_session_id session-id)]
    (db-util/insert! conn :events db-event)))

(defn- find-ext-values [data]
  (filter (fn [[k v]]
            (.startsWith (str (name k)) "X-"))
          data))

(defn get-ext-type [conn type]
  (db-util/get-row conn :event_extension_types ["name = ?" (str (name type)) ] ))

(defn create-ext-type! [conn type & [description]]
  (db-util/insert! conn :event_extension_types {:name (str (name type)) :description description}))

(defn get-or-create-ext-type! [conn name & description]
  (util/try-times
    3 50
    (let [existing-type (get-ext-type conn name)]
      (if existing-type
        existing-type
        (create-ext-type! conn name description)))))

(defn- create-ext-value! [conn event key value]
  (let [event-id (:id event event)
        ext-type (get-or-create-ext-type! conn key)
        ext-type-id (:id ext-type)]
    (db-util/insert! conn :event_extension_values
             {:event_id event-id
              :event_extension_type_id ext-type-id
              :value value})))

;; add triggers to update updated_at fields to every table
(defn update-tracker-activity! [conn tracker-id timestamp]
  ;; TODO setting to event-time is correct?
  (jdbc/execute! conn
                 ["update trackers set latest_activity = greatest(?, latest_activity), updated_at = ? where id = ?"
                  (time-conv/to-sql-time timestamp) (db-util/current-sql-timestamp) tracker-id] ))

(defn store-event! [db tracker event]
  (jdbc/with-db-transaction
    [conn db]
    (let [store-time (db-util/current-sql-timestamp)
          event-time (get event :event_time store-time)
          event (assoc event :event_time event-time)
          tracker-id (:id tracker)
          session-code (:session_code event "default")
          session (get-or-create-session! conn tracker-id session-code event-time)
          new-event (create-event-row! conn tracker-id (:id session) event)
          ext-values (find-ext-values event)
          annotation (:annotation event)]

      (dorun
        (map (fn [[key val]]
               (create-ext-value! conn new-event key val))
             ext-values))

      ;v(when annotation
      ;;  (dao/create-annotation! conn new-event annotation))

      (update-tracker-activity! conn tracker-id event-time)
      ;; TODO convert event to structural, API format
      ;;(pubsub/broadcast! pubsub-service :tracker tracker-id event)
      new-event
      )))