(ns user
  (:require [clojure.repl :refer :all]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.test :refer [run-all-tests]]
            [clojure.spec.alpha :as spec]
            [clojure.java.io :as io]
            [clojure.spec.gen.alpha :as gen]
            [com.example.csv-service.data :as data]))

(defn run-tests []
  (run-all-tests #"com\.example\.csv-service\..*-test"))

(defn gen-data []
  (gen/generate (spec/gen ::data/csv-data)))



(comment
  (with-open [rdr (io/reader "data/sample_pipes.csv")]
    (->> (data/read rdr #" \| ")
         (doall))))
