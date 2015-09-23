(ns ruuvi.database.user-repository
  (:require
    [clojure.tools.logging :refer [trace debug info warn] :as log]
    [ruuvi.database.db-util :as db-util]
    [clojure.string :as s]
    [clojure.java.jdbc :as jdbc]
    [buddy.hashers :as hs])
  )

(defn- clean-string [s]
  (if (nil? s)
    s
    (-> s s/trim s/lower-case)))

(defn create-user! [conn user]
  (let [password (-> user :password)
        user (-> user
                 (select-keys [:username :name :email])
                 (update-in [:username] clean-string)
                 (update-in [:email] clean-string))
        user (assoc user :password_hash (hs/encrypt password))]
    (db-util/insert! conn :users user)))

(defn- update-last-login! [conn user]
  ;; TODO pass to this some genering func in db-util
  (jdbc/execute! conn
                 ["update users set prev_login = last_login, last_login = ? where id = ?"
                  (db-util/sql-now) (user :id)]))

(defn- handle-user-authenticated [conn user]
  (info "User" (-> user :username) "authenticated successfully.")
  (update-last-login! conn user)
  true)

(defn- handle-auth-fail [conn user]
  (info "User" (-> user :username) "failed to authenticate. Bad password.")
  false)

(defn- handle-user-found [conn user password]
  ;; hs/check is slow on purpose (e.g. 400 millisec)
  (if (hs/check password (-> user :password_hash))
    (handle-user-authenticated conn user)
    (handle-auth-fail conn user)))

(defn- handle-user-not-found
  [conn username password]
  (info "User" username "tried to login. User not found.")
  ;; authentication should be equally slow when user is not found
  (hs/encrypt password)
  false)

(defn authenticate-user
  "Authenticates user"
  [conn username password]
  (let [username (clean-string username)
        user (db-util/get-row-insecure conn :users ["email = ? or username = ?" username username])]
    (if user
      (handle-user-found conn user password)
      (handle-user-not-found conn username password))))

(defn get-user [conn username]
  (let [username (clean-string username)]
    (db-util/get-row conn :users ["email = ? or username = ?"
                                  username username])))
