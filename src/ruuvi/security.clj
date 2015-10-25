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
    [clojure.set :refer [rename-keys]]
    )
  (:import
    [javax.crypto Mac]
    [javax.crypto.spec SecretKeySpec]
    [org.apache.commons.codec.binary Hex])
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
  (-> user
      (select-keys [:id])
      (rename-keys {:id :user-id})))

;; TODO contains claims in plain text, consider encrypting
;; TODO return user object in the response
;; TODO add iss field to sign (issuer, e.g. http://ruuvi-server.fi)
(defn create-auth-token [auth-conf user]
  (let [now (t/now)
        expires (-> (t/plus now (t/days 1)) (util/to-timestamp))
        created (-> now (util/to-timestamp))]
    ;; see https://tools.ietf.org/html/rfc7519#section-4.1
    (jws/sign (user-claims user)
              (private-key auth-conf)
              {:alg :rs256
               :iss "ruuvi-server"
               :exp expires
               :sub (-> user :username)
               :iat created})
    )
  )


(defn auth-backend [auth-conf]
  (jws-backend {:secret   (public-key auth-conf)
                :token    "Token"
                :options  {:alg :rs256}
                :on-error (fn [_ err]
                            (throw+ err))
                }))

(defn generate-mac-message [params mac-field]
  (let [;; remove mac key
        non-mac-params (dissoc params mac-field)
        ;; sort keys alphabetically
        sorted-keys (sort (keys non-mac-params))
        ;; make included-keys a vector and convert to non-lazy list
        param-keys (vec sorted-keys)]
    ;; concatenate keys, values and separators
    (apply str (for [k param-keys]
                 (str (name k) ":" (params k) "|")))))

(defn compute-hmac [secret mac-message]
  (let [algorithm "HmacSHA1"
        mac (Mac/getInstance algorithm)
        secret-key (SecretKeySpec. (.getBytes secret "ASCII") algorithm)]

    (.init mac secret-key)
    (let [computed-hmac (.doFinal mac (.getBytes mac-message "ASCII"))
          computed-hmac-hex (Hex/encodeHexString computed-hmac)]
      computed-hmac-hex
      )))
