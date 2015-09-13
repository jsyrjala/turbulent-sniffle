(ns ruuvi.security
  "Security services"
  (:require
   [buddy.hashers :as hs]
   [ruuvi.resources.domain :as domain]
   [clojure.tools.logging :as log :refer [info debug]]
   [ring.util.http-response :as r]
   [buddy.sign.jws :as jws]
   [buddy.sign.util :as util]
   [buddy.core.keys :as ks]
   [buddy.auth.backends.token :refer [jws-backend]]
   [clj-time.core :as t]
   [clojure.java.io :as io]
   )
  )

(defn- private-key* [auth-conf]
  (ks/private-key (io/resource (:private-key auth-conf))
                  (:passphrase auth-conf))
  )

(defn- public-key* [auth-conf]
  (ks/public-key (io/resource (:public-key auth-conf)))
  )

(def private-key (memoize private-key*))
(def public-key (memoize public-key*))

(defn user-assertions [user]
  (dissoc user :password)
  )

(defn create-auth-token [auth-conf assertions]
  (let [expires (-> (t/plus (t/now) (t/days 1)) (util/to-timestamp))]
    {:token (jws/sign assertions
                      (private-key auth-conf)
                      {:alg :rs256 :exp expires})}
    )
  )


(defn auth-backend[auth-conf]
  (jws-backend {:secret (public-key auth-conf)}))
