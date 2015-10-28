(ns foo
    (:require
      [user]
      [clojure.tools.namespace.repl :refer [refresh]]
      ))

(user/reset)

(user/stop)
(user/database-reset)
