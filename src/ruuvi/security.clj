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
   [slingshot.slingshot :refer [throw+]]
   )
  )

(defn- private-key* [auth-conf]
  (ks/private-key (io/reader (:private-key auth-conf))
                  (:passphrase auth-conf))
  )

(defn- public-key* [auth-conf]
  (ks/public-key (io/reader (:public-key auth-conf)))
  )

(def private-key (memoize private-key*))

(def public-key (memoize public-key*))

(defn user-claims [user]
  (dissoc user :password)
  )

;; TODO contains claims in plain text
(defn create-auth-token [auth-conf claims]
  (let [now (t/now)
        expires (-> (t/plus now (t/days 1)) (util/to-timestamp))
        created (-> now (util/to-timestamp))]
    ;; see https://tools.ietf.org/html/rfc7519#section-4.1
    {:token (jws/sign claims
                      (private-key auth-conf)
                      {:alg :rs256
                       :exp expires
                       :sub (-> claims :username)
                       :iat created })}
    )
  )


(defn auth-backend[auth-conf]
  (jws-backend {:secret (public-key auth-conf)
                :token "Token"
                :options {:alg :rs256}
                :on-error (fn [req err]
                            (throw+ err))
                }))
