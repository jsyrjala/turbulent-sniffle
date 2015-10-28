(ns ruuvi.resources.events
  "Events"
  {:swagger-tag "Events"
   :swagger-summary "Event related operations"}
  (:require [ring.util.response :as r]
            [clojure.tools.logging :as log :refer [info debug]]
            [ruuvi.resources.domain :as domain]
            [ruuvi.database.tracker-repository :as trackers]
            [ruuvi.database.event-repository :as events]
            ))

(defn- get-tracker [db event]
  (let [tracker-code (-> event :tracker_code)]
    (trackers/get-tracker db tracker-code)))

(defn- tracker-owned-by-user? [tracker identity]
  (= (-> tracker :owner_id) (-> identity :user-id)))

(defn- authenticated-tracker? [db tracker event]
  (trackers/authenticate-tracker db event)
  )

(defn- not-authenticated [tracker]
  false)

(defn- authenticated [tracker event]
  true)

(defn- tracker-not-exists-response [event]
  (r/response {:error :tracker-not-exists
               :description "Tracker does not exist"}))

(defn- not-authenticated-response [event]
  (r/response {:error :not-authenticated
               :description "Not authenticated"}))

(defn- store-event [db tracker event]
  (let [new-event (events/store-event! db tracker event)])
  (r/response {:success "ok"
               :event {:id (:id new-event)}}))

(defn- jwt-authenticated? [identity tracker]
  "Check that logged in user owns the tracker"
  (cond (not identity) false
        (tracker-owned-by-user? tracker identity) true
        :default (not-authenticated tracker)))

(defn- tracker-authenticated?
  "Check HMAC and tracker password authentication."
  [db tracker event]
  (cond (authenticated-tracker? db tracker event) (authenticated tracker event)
        :default (not-authenticated tracker)
        )
  )

(defn create
  "Create a new event."
  {:summary "Create a new event. Either JWT authentication token or tracker authentication required."
   :body-schema domain/NewEvent}
  [^:request-key body-params
   ^:request-key identity
   ^:injection db]
  (let [event body-params
        tracker (get-tracker db event)]
    (cond
      (not tracker) (tracker-not-exists-response event)
      (jwt-authenticated? identity tracker) (store-event db tracker event)
      (tracker-authenticated? db tracker event) (store-event db tracker event)
      :default (not-authenticated-response event))
    )
  )
