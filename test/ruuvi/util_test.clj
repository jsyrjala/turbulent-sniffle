(ns ruuvi.util-test
  (:require
    [ruuvi.util :refer :all]
    [midje.sweet :refer :all]
    )
  )

(fact
  "process-id returns process id"
  (let [pid (process-id)]
    pid => #(> % 0)
    ))










