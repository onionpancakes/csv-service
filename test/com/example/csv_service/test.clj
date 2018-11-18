(ns com.example.csv-service.test
  (:require [clojure.test :refer [run-all-tests]]
            [com.example.csv-service.data-test]
            [com.example.csv-service.data-spec-test]
            [com.example.csv-service.server-test]))

(defn run-tests []
  (run-all-tests #"com\.example\.csv-service\..*-test"))

(defn run-serv-tests []
  (run-all-tests #"com\.example\.csv-service\.server-test"))

