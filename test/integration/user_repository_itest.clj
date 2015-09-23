(ns integration.user-repository-itest
  (:require
    [ruuvi.database.user-repository :refer :all]
    [midje.sweet :refer :all]
    [ruuvi.test-util :as test-util :refer :all]
    ))

(defn not-nil? [a]
  (not= (nil? a)))

(against-background
  [(before
     :contents
     (test-util/init-system "test/data/system-config.edn"))
   (after
     :contents
     (test-util/stop-system))
   ]

  (fact "create-user! creates an user"
        (let [user (create-user! (-> system :db) {:username "pete" :password "verysecret!"
                                                  :email "pete@example.com"})]
          user => (contains {:id integer? :username "pete" :email "pete@example.com" :created_at anything :updated_at anything})
          (keys user) =>  (just #{:id :username :email :created_at :updated_at}) ))

  (fact "create-user! throws exception when trying to create duplicate username"
        (create-user! (-> system :db) {:username "pete" :password "hubbabubba"
                                       :email "somebodyother@example.com"})
        => (throws org.h2.jdbc.JdbcSQLException #"Unique index or primary key violation: \"UIX_USERS_USERNAME ON PUBLIC.USERS"))

  (fact "create-user! throws exception when trying to create duplicate email"
        (create-user! (-> system :db) {:username "john" :password "hubbabubba"
                                       :email "pete@example.com"})
        => (throws org.h2.jdbc.JdbcSQLException #"Unique index or primary key violation: \"UIX_USERS_EMAIL ON PUBLIC.USERS"))


  (fact "get-user has last_login"
        (let [user (get-user (-> system :db) "pete")]
          user => (just {:id anything :username "pete" :email "pete@example.com"
                         :created_at anything :updated_at anything})))

  (fact "authenticate-user authenticate when given correct email and password"
        (authenticate-user (-> system :db) "pete@example.com" "verysecret!") => true)

  (fact "get-user has last_login and prev_login"
        (let [user (get-user (-> system :db) "pete")]
          user => (just {:id truthy :username "pete" :email "pete@example.com"
                         :created_at anything :updated_at anything
                         :last_login anything})))

  (fact "authenticate-user authenticate when given correct username and password"
        (authenticate-user (-> system :db) "pete" "verysecret!") => true)

  (fact "authenticate-user authenticate ignores white space in username"
        (authenticate-user (-> system :db) " pete \t" "verysecret!") => true)

  (fact "authenticate-user authenticate when given correct username with case variation"
        (authenticate-user (-> system :db) "PeTe" "verysecret!") => true)

  (fact "authenticate-user authenticate when given correct email with case variation and password"
        (authenticate-user (-> system :db) "PeTe@eXaMpLe.CoM" "verysecret!") => true)

  (fact "authenticate-user doesn't authenticate when given correct username with bad password"
        (authenticate-user (-> system :db) "pete" "wrongpass?") => false)

  (fact "authenticate-user doesn't authenticate when given correct email with bad password"
        (authenticate-user (-> system :db) "pete@example.com" "wrongpass?") => false)

  (fact "authenticate-user doesn't authenticate when user is not found"
        (authenticate-user (-> system :db) "john@example.com" "verysecret!") => false)

  (fact "authenticate-user doesn't authenticate when given nil password"
        (authenticate-user (-> system :db) "pete@example.com" nil) => false)

  )




