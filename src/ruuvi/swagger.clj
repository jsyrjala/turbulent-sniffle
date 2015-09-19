(ns ruuvi.swagger
  "Swagger middleware and configuration."
  (:require
   [clojure.tools.logging :as log :refer [info]]
   [io.aviso.rook.swagger :as sw]
   [ring.util.response :as r]
   [schema.core :as s]
   [io.aviso.rook.swagger :refer [SwaggerObject SwaggerOptions RoutingEntry PathItem]]
   [ring.middleware.content-type :refer [content-type-response]]
   [ring.middleware.not-modified :refer [not-modified-response]]
   [ring.middleware.head :refer [head-response]]))

(defn- routing-tag [routing-entry]
  "Get tag name from namespace metadata :swagger-tag.
  Use namespace name as default."
  (let [ns (-> routing-entry :meta :ns)
        tag (or
             (-> ns meta :swagger-tag)
             (ns-name ns))]
    tag))

(defn- routing-tag-summary [routing-entry]
  "Get tag description from namespace metadata :swagger-summary.
  Use namespace doc string as default."
  (let [ns (-> routing-entry :meta :ns)
        tag (or
             (-> ns meta :swagger-summary)
             (-> ns meta :doc))]
    tag))

(defn- routing-tags [routing-entries]
  (let [tags (map (fn [routing-entry]
                    {:name (routing-tag routing-entry)
                     :description (routing-tag-summary routing-entry)}
                    ) routing-entries)]
    (->> (group-by :name tags)
         vals
         (map first))))

(s/defn tagging-configurer :- SwaggerObject
  "Decorates a SwaggerObject with the tags and descriptions.

   Routes with same tag are grouped as a one block in Swagger UI."
  [swagger-options :- SwaggerOptions
   swagger-object :- SwaggerObject
   routing-entries :- [RoutingEntry]]
  (assoc swagger-object :tags (routing-tags routing-entries)))

(s/defn tagging-operation-decorator :- PathItem
  "Decorates a PathItemObject (route) with a tag.
   Routes with same tag are grouped to same block in the Swagger UI."
  [swagger-options :- SwaggerOptions
   swagger-object :- SwaggerObject
   routing-entry :- RoutingEntry
   path-item-object :- PathItem]
  (merge path-item-object {:tags [(routing-tag routing-entry)]}))

(defn- swagger-ui-response
  "Reads and serves files for Swagger UI website from ring-swagger-ui.jar"
  [uri prefix swagger-path]
  (let [base "/swagger-ui"
        req-path (.replaceAll uri (str "^" prefix) "")]
    (condp = req-path
      "" (r/redirect (str uri "/"))
      "/" (-> (r/resource-response (str base "/index.html"))
              (r/update-header "Content-Type" (constantly "text/html")))
      "/conf.js" (-> (r/response (str "window.API_CONF = {url: '" swagger-path "'};"))
                     (r/update-header "ETag" (constantly swagger-path) ))
      (r/resource-response (str base req-path)))))

(defn wrap-swagger-ui
  "Middleware for serving Swagger UI web page.
  The web page is served for all URLs that start with prefix."
  [handler prefix swagger-path]
  (fn [{:keys [:uri] :as req}]
    (if (.startsWith uri prefix)
      (->
       (swagger-ui-response uri prefix swagger-path)
       (not-modified-response req)
       (content-type-response req)
       (head-response req))
      (handler req))))

(def swagger-options
  (-> sw/default-swagger-options
      (update-in [:template :info] merge {:title "RuuviTracker"
                                          :version "Pre-Alpha"
                                          :description "RuuviTracker API"
                                          :contact {:email "foo@bar.fi"}
                                          :license {:name "Eclipse Public License - v 1.0"
                                                    :url "https://www.eclipse.org/legal/epl-v10.html"}
                                          })
      (update-in [:template] merge {:produces ["application/json"]
                                    :consumes ["application/json"]
                                    :host ""
                                    :basePath ""
                                    })
      (merge {:operation-decorator tagging-operation-decorator
              :configurer tagging-configurer})))
