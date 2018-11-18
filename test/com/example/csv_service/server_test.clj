(ns com.example.csv-service.server-test
  (:require [clojure.test :refer [deftest is]]
            [io.pedestal.http :as http]
            [clj-http.client :as client]
            [com.example.csv-service.server :as serv]))

(defonce server
  (->> {:env          :dev
        ::http/join?  false
        ::http/routes #(deref #'serv/routes)
        ::http/port   9000}
       (merge serv/service)
       (serv/create-server)
       (http/start)))

(defn get-hello []
  (slurp "http://localhost:9000/hello"))

(deftest test-hello
  (is (= (get-hello) "Hello World!")))


