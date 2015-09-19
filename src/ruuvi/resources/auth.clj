(ns ruuvi.resources.auth
  "Authentication resource"
  {:swagger-tag "Authentication"
   :swagger-summary "Resources for obtaining and revoking authentication tokens. I.e login/logout."}
  (:require
   [ruuvi.resources.domain :as domain]
   [ruuvi.security :as sec]
   [buddy.hashers :as hs]
   [clojure.tools.logging :as log :refer [info debug]]
   [ring.util.http-response :as r]
   [ring.util.http-status :as status]
   )
  )

(defn- make-user [username password]
  {:username username
   :password (hs/encrypt password)})

(def user-db [(make-user "john" "jj")
              (make-user "jim" "passwd")
              ]
  )


(defn find-user [username]
  (first (filter (fn [user]
            (= (-> user :username) username))
          user-db))
  )

(defn- auth-failed [username reason]
  (info "Authentication failed for user" username ":" reason)
  (r/unauthorized {:error "unauthorized" :description "Bad credentials"})
  )

(defn- auth-success [user auth-conf]
  (let [claims (sec/user-claims user)
        token (sec/create-auth-token auth-conf claims)
        username (:username user)]
    (info "Authentication success for user" username)
    (r/created token)
    ))

(defn login
  "Authenticate and obtain auth token"
  {:summary "Authenticate and obtain auth token"
   :route [:post]
   :body-schema domain/Authentication
   :responses {status/ok domain/AuthToken
               status/unauthorized domain/ErrorResponse
               }}
  [^:request-key body-params
   ^:injection auth-conf
   ^:injection db]
  (let [username (-> body-params :username)
        password (-> body-params :password)
        user (find-user username)]
    (if user
      (if (hs/check password (-> user :password))
        (auth-success user auth-conf)
        (auth-failed username "Bad password")
        )
      (auth-failed username "Unknown user")
      )
    )
  )

;; do logout later
