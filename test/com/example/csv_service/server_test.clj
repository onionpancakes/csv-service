(ns com.example.csv-service.server-test
  (:require [clojure.test :refer [deftest is]]
            [io.pedestal.http :as http]
            [clj-http.client :as client]
            [com.example.csv-service.server :as serv]))

(defonce state
  (atom serv/initial-state))

(defonce server
  (-> {:env          :dev
       ::http/join?  false
       ::http/routes #(deref #'serv/routes)
       ::http/port   9000}
      (as-> x (merge serv/service x))
      (serv/create-server state)
      (http/start)))

(defn get-hello []
  (slurp "http://localhost:9000/hello"))

(deftest test-hello
  (is (= (get-hello) "Hello World!")))


