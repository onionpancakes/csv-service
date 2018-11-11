(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.test :refer [run-all-tests]]
            [com.example.csv-service.data :as data]))

(defn run-tests []
  (run-all-tests #"com\.example\.csv-service\..*-test"))

(defn foo []
  :foo)

