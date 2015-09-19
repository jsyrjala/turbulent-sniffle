(ns ruuvi.resources.domain
  [:require [schema.core :as s :refer [optional-key Int Str enum Any defschema]]]
)

;; Meta

(defschema Pong
  {:server String
   :version String
   :server_time java.util.Date})

;; Common

(defschema ErrorResponse
  {:error String
   (optional-key :description) String
   Any Any})

;; Events
(defschema NewEvent
  {:version (enum "1")
   :tracker_code Str
   (optional-key :session_code) Str
   (optional-key :time) String
   (optional-key :nonce) String
   ;; TODO support also decimal
   (optional-key :latitude) String
   (optional-key :longitude) String
   (optional-key :accuracy) Double
   (optional-key :vertical_accuracy) Double
   (optional-key :heading) Double
   (optional-key :satellite_count) Long
   (optional-key :battery) Double
   (optional-key :speed) Double
   (optional-key :altitude) Double
   (optional-key :temperature) Double
   (optional-key :annotation) String
   (optional-key :mac) String
   Any                   Any
   }
  )

;; Auth
(defschema Authentication
  {:username Str
   :password Str})

(defschema AuthToken
  {:token Str})


