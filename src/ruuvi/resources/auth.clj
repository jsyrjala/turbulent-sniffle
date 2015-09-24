(ns ruuvi.resources.auth
  "Authentication resources"
  {:swagger-tag "Authentication"
   :swagger-summary "Resources for obtaining and revoking authentication tokens. I.e login/logout."}
  (:require
   [ruuvi.resources.domain :as domain]
   [ruuvi.security :as sec]
   [buddy.hashers :as hs]
   [clojure.tools.logging :refer [info debug]]
   [ring.util.http-response :as r]
   [ring.util.http-status :as status]
   [ruuvi.database.user-repository :as users]
   ))

(defn- auth-failed []
  (r/unauthorized {:error "unauthorized"
                   :description "Bad credentials"}) )

(defn- auth-success [user auth-conf]
  (let [claims (sec/user-claims user)
        token (sec/create-auth-token auth-conf claims)]
    (r/created token) ))

(defn login
  "Authenticate and obtain auth token"
  {:summary "Authenticate and obtain auth token"
   :route [:post]
   :body-schema domain/Authentication
   :responses {status/ok domain/AuthToken
               status/unauthorized domain/ErrorResponse }}
  [^:request-key body-params
   ^:injection auth-conf
   ^:injection db]
  (let [username (-> body-params :username)
        password (-> body-params :password)
        user (users/authenticate-user db username password)]
    (if user
      (auth-success user auth-conf)
      (auth-failed) )))

;; do logout later
