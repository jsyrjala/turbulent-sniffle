(ns ruuvi.database.db-util
  (:require
    [java-jdbc.sql :as sql]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :refer [trace debug info warn] :as log]
    [clj-time.coerce :as time-conv]
    [clj-time.core :as time]
    )
  )

(defn sql-now
  "Return current time as sql timestamp"
  []
  (-> (time/now)
      time-conv/to-timestamp))

(defn to-sql-data
  "Converts domain data to database/jdbc compatible format.

  E.g convert DateTiem to sql Timestamps"
  [domain-map]
  (into (array-map)
        (map (fn mapper [[k v]]
               (if (instance? org.joda.time.DateTime v)
                 [k (time-conv/to-sql-time v)]
                 [k v]))
             domain-map)))

(defn map-func
  ;; TODO better doc
  "When data is a sequence map-func is equal to map.
  Otherwise map-func is equal to func."
  [func data]
  (cond
    (vector? data) (map func data)
    (seq? data) (map func data)
    :default (func data)))


(defn remove-nils
  "Remove key-values that have nil values"
  [data-map]
  (let [data (into {}
                   (filter
                     (fn [item]
                       (if item
                         (let [value (item 1)]
                           (cond (and (coll? value) (empty? value)) false
                                 (= value nil) false
                                 :else true))
                         nil)
                       ) data-map))]
    (if (empty? data)
      nil
      data)))

(defn- log-clean
  "Hides sensitive data (e.g. password_hash) while logging"
  [sql-data]
  (if (sql-data :password_hash)
    (assoc sql-data :password_hash "<secret>")
    sql-data))

(defn- to-domain-data
  "Converts database objects to domain objects

  E.g convert sql Timestamps to DateTime objects."
  [sql-map]
  (into (array-map)
        (map (fn mapper [[k v]]
               (cond (instance? java.sql.Timestamp v)
                     [k (time-conv/from-sql-time v)]
                     (instance? BigDecimal v)
                     [k (double v)]
                     :default [k v]))
             sql-map)))


(defn to-domain [m]
  (if (nil? m)
    nil
    (map-func
      #(-> %
           remove-nils
           to-domain-data) m)))

(defn get-row-insecure [conn table predv]
  "Like get-row but doesn't remove sensitive information."
  (->
    (jdbc/query conn (sql/select * table predv))
    first
    to-domain))

(defn get-row
  "Fetch a single row from database. Removes any sensitive
  fields like password hashes. If predv matches several rows,
  only the first is returned.

  E.g (get-row db :users [\"email = ?\" \"foo@example.com\")
  "
  [conn table predv]
  (dissoc (get-row-insecure conn table predv) :password_hash :password))

(defn get-by-id [conn table id]
  (get-row conn table ["id = ?" id]))

(defn insert! [conn table data]
  (let [sql-data (to-sql-data data)
        _   (debug "insert!" table (log-clean sql-data))
        row (first (jdbc/insert! conn table sql-data))
        id-keys [(keyword "scope_identity()")
                 (keyword "SCOPE_IDENTITY()")
                 :id]
        id (first (filter identity (map row id-keys)))]
    ;; return value for H2 is just {:scope_identity() <id>}
    ;; so make a query to fetch full row
    ;; TODO optimize: some dbs return full row, return it directly
    ;; you need this to get created_at and updated_at fields
    (to-domain (get-by-id conn table id))))







