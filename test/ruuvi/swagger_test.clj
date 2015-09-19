(ns ruuvi.swagger-test
  (:require
    [ruuvi.swagger :refer :all]
    [midje.sweet :refer :all]))

(defn- has-content [data]
  (and (not (nil? data) ) (not= data "") ))

(fact "wrap-swagger-ui"
      (fact "redirects /doc to swagger-ui/index.html"
            ((wrap-swagger-ui identity "/doc" "/api/swagger.json")
              {:uri "/doc"} )
            => (contains  {:body ""
                           :status 302
                           :headers {"Content-Type" "application/octet-stream",
                                     "Location" "/doc/"}} ))

      (fact "resolves /doc/ to swagger-ui/index.html"
            (let [result ((wrap-swagger-ui identity "/doc" "/api/swagger.json")
                           {:uri "/doc/"} )]
              result => (contains {:status 200 :body has-content} )
              (result :headers) => (contains {"Content-Type" "text/html"})))

      (fact "resolves /doc/index.html to swagger-ui/index.html"
            (let [result ((wrap-swagger-ui identity "/doc" "/api/swagger.json")
                           {:uri "/doc/index.html"} )]
              result => (contains {:status 200 :body has-content} )
              (result :headers) => (contains {"Content-Type" "text/html"})))

      (fact "resolves /doc/conf.js to dynamically generated conf.js with correct swagger path"
            (let [result ((wrap-swagger-ui identity "/doc" "/api/swagger.json")
                           {:uri "/doc/conf.js"} )]
              result => (contains {:status 200
                                   :body "window.API_CONF = {url: '/api/swagger.json'};"} )
              (result :headers) => (contains {"Content-Type" "text/javascript"
                                              "ETag" "/api/swagger.json"}))))
