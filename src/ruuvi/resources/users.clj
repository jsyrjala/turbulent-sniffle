(ns ruuvi.resources.users
  "User resources"
  {:swagger-tag "Users"
   :swagger-summary "Resources querying, registering and managing users."}
  (:require
    [ruuvi.resources.domain :as domain]
    [ruuvi.security :as sec]
    [buddy.hashers :as hs]
    [clojure.tools.logging :refer [info debug]]
    [ring.util.http-response :as r]
    [ring.util.http-status :as status]
    [ruuvi.database.user-repository :as users]))

(defn create
  "Create a new user"
  {:summary ""
   :route [:put]
   :body-schema domain/NewUser
   :responses {status/ok domain/User}}
   [^:request-key body-params
    ^:injection db]
  (r/created (users/create-user! db body-params)))