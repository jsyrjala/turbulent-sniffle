(ns integration.user-repository-itest
  (:require
    [ruuvi.database.user-repository :refer :all]
    [midje.sweet :refer :all]
    [ruuvi.test-util :as test-util :refer :all]
    ))

(against-background
  [(before
     :contents
     (test-util/init-system "test/data/system-config.edn"))
   (after
     :contents
     (test-util/stop-system))]

  (fact "create-user! creates an user"
        (let [user (create-user! (db) {:username "pete" :password "verysecret!"
                                                  :email "pete@example.com"})]
          user => (contains {:id integer? :username "pete" :email "pete@example.com" :created_at anything :updated_at anything})
          (keys user) =>  (just #{:id :username :email :created_at :updated_at :failed_login_count}) ))

  (fact "get-user returns the user"
        (let [user (get-user (db) "pete")]
          user => (just {:id anything :username "pete" :email "pete@example.com"
                         :created_at anything :updated_at anything :failed_login_count 0})))

  (fact "create-user! throws exception when trying to create duplicate username"
        (create-user! (db) {:username "pete" :password "hubbabubba"
                                       :email "somebodyother@example.com"})
        => (throws org.h2.jdbc.JdbcSQLException #"Unique index or primary key violation: \"UIX_USERS_USERNAME ON PUBLIC.USERS"))

  (fact "create-user! throws exception when trying to create duplicate email"
        (create-user! (db) {:username "john" :password "hubbabubba"
                                       :email "pete@example.com"})
        => (throws org.h2.jdbc.JdbcSQLException #"Unique index or primary key violation: \"UIX_USERS_EMAIL ON PUBLIC.USERS"))

  (fact "authenticate-user authenticate when given correct username and password"
        (authenticate-user (db) "pete" "verysecret!") => truthy)

  (fact "get-user has last_login"
        (let [user (get-user (db) "pete")]
          user => (just {:id anything :username "pete" :email "pete@example.com"
                         :created_at anything :updated_at anything :failed_login_count 0
                         :last_login anything})))

  (fact "authenticate-user authenticate when given correct email and password"
        (authenticate-user (db) "pete@example.com" "verysecret!") => truthy)

  (fact "get-user has last_login and prev_login"
        (let [user (get-user (db) "pete")]
          user => (just {:id anything :username "pete" :email "pete@example.com"
                         :created_at anything :updated_at anything
                         :failed_login_count 0
                         :last_login anything :prev_login anything})))

  (fact "authenticate-user authenticate ignores white space in username"
        (authenticate-user (db) " pete \t" "verysecret!") => truthy)

  (fact "authenticate-user authenticate when given correct email with case variation and password"
        (authenticate-user (db) "PeTe@eXaMpLe.CoM" "verysecret!") => truthy)

  (fact "authenticate-user doesn't authenticate when given correct username with bad password"
        (authenticate-user (db) "pete" "wrongpass?") => false)

  (fact "get-user has failed login count 1 after failed login"
        (let [user (get-user (db) "pete")]
          user => (just {:id anything :username "pete" :email "pete@example.com"
                         :created_at anything :updated_at anything
                         :last_login anything :prev_login anything
                         :failed_login_count 1
                         :last_failed_login anything})
          (fact "get-user-by returns same object as get-user"
                (get-user-by-id (db) (:id user)) => user )))

  (fact "authenticate-user doesn't authenticate when given correct email with bad password"
        (authenticate-user (db) "pete@example.com" "wrongpass?") => false)

  (fact "get-user has failed login count 2 after second failed login"
        (let [user (get-user (db) "pete")]
          user => (just {:id truthy :username "pete" :email "pete@example.com"
                         :created_at anything :updated_at anything
                         :last_login anything :prev_login anything
                         :failed_login_count 2 :last_failed_login anything})))

  (fact "authenticate-user doesn't authenticate when user is not found"
        (authenticate-user (db) "john@example.com" "verysecret!") => false)

  (fact "authenticate-user doesn't authenticate when given nil password"
        (authenticate-user (db) "pete@example.com" nil) => false)

  (fact "authenticate-user authenticate when given correct username with case variation"
        (authenticate-user (db) "PeTe" "verysecret!") => truthy)

  (fact "get-user has failed login count 0 after successful login"
        (let [user (get-user (db) "pete")]
          user => (just {:id truthy :username "pete" :email "pete@example.com"
                         :created_at anything :updated_at anything
                         :last_login anything :prev_login anything
                         :failed_login_count 0 :last_failed_login anything})))
  )
