(ns ruuvi.resources.domain
  [:require [schema.core :as s :refer [optional-key Int Str enum Any defschema]]]
  )

;; Meta

(defschema Pong
  {:server      String
   :version     String
   :server_time java.util.Date})

;; Common

(defschema ErrorResponse
  {:error                      String
   (optional-key :description) String
   :request_id                 String
   :server_time                java.util.Date
   Any                         Any})

;; Events
(defschema NewEvent
  {:version                          (enum "1")
   :tracker_code                     Str
   (optional-key :session_code)      Str
   (optional-key :time)              java.util.Date
   (optional-key :nonce)             String
   ;; TODO support also decimal
   (optional-key :latitude)          String
   (optional-key :longitude)         String
   (optional-key :accuracy)          Double
   (optional-key :vertical_accuracy) Double
   (optional-key :heading)           Double
   (optional-key :satellite_count)   Long
   (optional-key :battery)           Double
   (optional-key :speed)             Double
   (optional-key :altitude)          Double
   (optional-key :temperature)       Double
   (optional-key :annotation)        String
   (optional-key :mac)               String
   (optional-key :password)          String
   Any                               Any
   }
  )

;; Users
(defschema NewUser
  {:username             Str
   :password             Str
   (optional-key :name)  Str
   (optional-key :email) Str})

(defschema User
  {:id                                Long
   :username                          Str
   (optional-key :name)               Str
   (optional-key :email)              Str
   (optional-key :created_at)         java.util.Date
   (optional-key :updated_at)         java.util.Date
   (optional-key :prev_login)         java.util.Date
   (optional-key :last_login)         java.util.Date
   (optional-key :failed_login_count) Int
   })

;; Trackers

(defschema Tracker
  {:id                             Long
   :tracker_code                   Str
   (optional-key :latest_activity) java.util.Date
   (optional-key :owner_id)        Long
   (optional-key :name)            Str
   (optional-key :description)     Str
   })

(defschema NewTracker
  {:tracker_code                 Str
   (optional-key :name)          Str
   (optional-key :description)   Str
   (optional-key :password)      Str
   (optional-key :shared_secret) Str
   })

;; Auth
(defschema Authentication
  {:username Str
   :password Str})

(defschema AuthToken
  {:token Str
   :user  User})


