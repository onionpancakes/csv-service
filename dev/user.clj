(ns user
  (:require [clojure.repl :refer :all]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.test :refer [run-all-tests]]
            [clojure.spec.alpha :as spec]
            [clojure.java.io :as io]
            [clojure.spec.gen.alpha :as gen]
            [com.example.csv-service.data :as data]
            [com.example.csv-service.data.spec :as data.spec]))

(defn run-tests []
  (run-all-tests #"com\.example\.csv-service\..*-test"))

(defn gen-data []
  (gen/generate (spec/gen ::data.spec/csv-data)))

(defn gen-random-sample []
  (with-open [w1 (io/writer "data/random_pipes.csv")
              w2 (io/writer "data/random_comma.csv")
              w3 (io/writer "data/random_space.csv")]
    (data/write w1 " | " (gen-data))
    (data/write w2 ", " (gen-data))
    (data/write w3 " " (gen-data))))

(comment
  (with-open [rdr (io/reader "data/sample_pipes.csv")]
    (->> (data/read rdr #" \| ")
         (doall))))
