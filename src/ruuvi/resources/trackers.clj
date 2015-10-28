(ns ruuvi.resources.trackers
  "Trackers"
  {:swagger-tag "Trackers"
   :swagger-summary "Tracker related operations"}
  (:require
    [ruuvi.resources.domain :as domain]
    [ruuvi.database.tracker-repository :as trackers]
    [clojure.tools.logging :as log :refer [info debug]]
    [ring.util.http-response :as r]
    [ring.util.http-status :as status]
    ))

(defn create
  "Create a tracker event."
  {:summary "Create a new event. Authentication required."
   :body-schema domain/NewTracker
   :responses {status/created domain/AuthToken}}
  [^:request-key body-params
   ^:request-key identity
   ^:injection db]
  (let [tracker body-params
        new-tracker (trackers/create-tracker! db {:id (-> identity :user-id)} tracker)]
    (r/created new-tracker)))
