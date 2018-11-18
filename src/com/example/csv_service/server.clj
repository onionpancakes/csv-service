(ns com.example.csv-service.server
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.interceptor :as i]))

(defn hello-world [req]
  {:status 200
   :content-type "text/plain"
   :body "Hello World!"})

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

(defn state-interceptor []
  {:name ::state
   :enter (fn [ctx]
            (assoc-in ctx [:request ::state] (atom nil)))})

(defn interceptors []
  [(i/interceptor (state-interceptor))])

(defn create-server [service]
  (-> service
      (http/default-interceptors)
      (http/dev-interceptors)
      (update ::http/interceptors (partial into (interceptors)))
      (http/create-server)))

(defonce server
  (create-server service))

(defn start []
  (http/start server))

(defn stop []
  (http/stop server))

(defn -main []
  (start))

