(ns com.example.csv-service.server
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]))

(defonce state (atom nil))

(defn hello-world [req]
  {:status 200 :body "Hello World!"})

;; TODO

;; Server

(def routes
  (route/expand-routes
   #{["/hello" :get hello-world :route-name :hello]
     ["/records/gender" :get hello-world :route-name :gender]
     ["/records/birthdate" :get hello-world :route-name :birthdate]
     ["/records/name" :get hello-world :route-name :name]}))

(def service
  {::http/routes routes
   ::http/type   :jetty
   ::http/port   8080})

(defonce server
  (http/create-server service))

(defn start []
  (http/start server))

(defn stop []
  (http/stop server))

(defn -main []
  (start))

