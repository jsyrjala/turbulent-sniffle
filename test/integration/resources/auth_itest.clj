(ns integration.resources.auth-itest
  (:require [midje.sweet :refer :all]
            [ruuvi.test-util :as test-util :refer :all]
            [ruuvi.database.user-repository :as users])
  )

(defn unauthorized? [exception]
  (let [data (-> exception .getData)]
    (and (= 401 (-> data :status))
         (= "{\"error\":\"unauthorized\",\"description\":\"Bad credentials\"}"
            (-> data :body)) )))

(against-background
  [(before
     :contents
     (test-util/init-system "test/data/system-config.edn" {:users true} ))
   (after
     :contents
     (test-util/stop-system))]

  (fact
    "POST /api/auth"
    (fact
      "returns with status 401 authenticating with non existing username"
      (post-url "/auth" {:username "does-not-exist" :password "bar"}) => (throws clojure.lang.ExceptionInfo
                                                                                 unauthorized?))
    (fact
      "returns with status 401 authenticating with bad password"
      (post-url "/auth" {:username "jim" :password "bar"}) => (throws clojure.lang.ExceptionInfo
                                                                      unauthorized?))
    (fact
      "returns auth token when authenticating with correct username and password"
      (let [{:keys [body]} (post-url "/auth" {:username "jim" :password "verysecret"})]
        body => (just {:token anything})
        ))
    (fact
      "returns auth token when authenticating with another correct username and password"
      (let [{:keys [body]} (post-url "/auth" {:username "john" :password "secretpass"})]
        body => (just {:token anything})
        ))
    )
  )
