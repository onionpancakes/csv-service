(ns com.example.csv-service.server-test
  (:require [clojure.test :refer [deftest is are]]
            [clojure.data.json :as json]
            [io.pedestal.http :as http]
            [clj-http.client :as client]
            [com.example.csv-service.server :as serv]
            [com.example.csv-service.part1 :as p1]
            [com.example.csv-service.data.spec :as ds])
  (:import [java.io StringWriter]))

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

;; HTTP

(defn get-hello []
  (slurp "http://localhost:9000/hello"))

(def supported-seps
  {"pipe"  " | "
   "comma" ", "
   "space" " "})

(defn post [sep csv-data]
  (let [sw   (StringWriter.)
        _    (p1/write (get supported-seps sep ",") sw csv-data)
        body (str sw)]
    (client/post "http://localhost:9000/records"
                 {:body             body
                  :query-params     {:sep sep}
                  :throw-exceptions false})))

(defn get-gender []
  (client/get "http://localhost:9000/records/gender"))

(defn get-dob []
  (client/get "http://localhost:9000/records/birthdate"))

(defn get-name []
  (client/get "http://localhost:9000/records/name"))

;; Data

(def data-empty
  {:header ds/header})

;; Tests

(deftest test-hello
  (is (= (get-hello) "Hello World!")))

(deftest test-content-type
  (are [x y] (= (get (:headers x) "Content-Type") y)
    (post "foo" nil)          "application/json"
    (post "pipe" nil)         "application/json"
    (post "comma" nil)        "application/json"
    (post "space" nil)        "application/json"
    (post "pipe" data-empty)  "application/json"
    (post "comma" data-empty) "application/json"
    (post "space" data-empty) "application/json"
    (get-gender)              "application/json"
    (get-dob)                 "application/json"
    (get-name)                "application/json"))

(deftest test-status
  (are [x y] (= (:status x) y)
    (post "foo" nil)          422
    (post "pipe" nil)         422
    (post "comma" nil)        422
    (post "space" nil)        422
    (post "pipe" data-empty)  200
    (post "comma" data-empty) 200
    (post "space" data-empty) 200
    (get-gender)              200
    (get-dob)                 200
    (get-name)                200))

