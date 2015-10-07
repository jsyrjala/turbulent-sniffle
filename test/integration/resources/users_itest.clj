(ns integration.resources.users-itest
  (:require [midje.sweet :refer :all]
            [ruuvi.test-util :as test-util :refer :all]
            [ruuvi.database.user-repository :as users])
  )

(def user1 {:username "william"
            :password "MyPassW"
            :name "William Wallace"
            :email "william@example.com"})

(def user2 {:username "kalle"
            :password "MyPassW"
            :name "Kalle Kustaa"
            :email "kalle@example.com"})

(defn http-error [status]
  (fn [exception]
    (let [data (.getData exception)]
      (= (-> data :status) status))))

(against-background
  [(before
     :contents
     (test-util/init-system "test/data/system-config.edn" {:users true} ))
   (after
     :contents
     (test-util/stop-system))]

  (fact "PUT /api/users"
        (fact "creates a user"
              (let [{:keys [body]} (test-util/put-url "/users" user1)]

                body => (contains {:username (user1 :username)
                                   :email    (user1 :email)
                                   :name     (user1 :name)})))
        (fact "doesn't create another user with same username"
             (test-util/put-url "/users"
                                {:username (user1 :username)
                                 :password "pw"})
              => (throws Exception))
        (fact "doesn't create another user with same username and case variation"
              (test-util/put-url "/users"
                                 {:username "WiLLiam"
                                  :password "pw"})
              => (throws Exception))
        (fact "doesn't create another user with same email"
              (test-util/put-url "/users"
                                 {:username "123"
                                  :password "pw"
                                  :email (user1 :email)})
              => (throws Exception))
        (fact "doesn't create another user with same email and case variation"
              (test-util/put-url "/users"
                                 {:username "123"
                                  :password "pw"
                                  :email "WiLLiam@eXample.Com"})
              => (throws Exception (http-error 500)))
        (fact "creates a user"
              (let [{:keys [body]} (test-util/put-url "/users" user2)]
                body => (contains {:username (user2 :username)
                                   :email    (user2 :email)
                                   :name     (user2 :name)})))
        )

  )