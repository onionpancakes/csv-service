(ns com.example.csv-service.server
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.interceptor :as i]
            [clojure.data.json :as json]))

(defn hello-world [req]
  {:status 200 :body "Hello World!"})

(defn gender-handler [{state ::state}]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (json/write-str @state)})

(defn dob-handler [{state ::state}]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (json/write-str @state)})

(defn name-handler [{state ::state}]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (json/write-str @state)})

;; TODO

;; Server

(def routes
  (route/expand-routes
   #{["/hello" :get hello-world :route-name :hello]
     ["/records/gender" :get gender-handler :route-name :gender]
     ["/records/birthdate" :get dob-handler :route-name :birthdate]
     ["/records/name" :get name-handler :route-name :name]}))

(def service
  {::http/routes routes
   ::http/type   :jetty
   ::http/port   8080})

(def initial-state
  {:headers ["LastName" "FirstName" "Gender" "FavoriteColor" "DateOfBirth"]})

(defn state-interceptor []
  {:name ::state
   :enter (fn [ctx]
            (assoc-in ctx [:request ::state] (atom initial-state)))})

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

