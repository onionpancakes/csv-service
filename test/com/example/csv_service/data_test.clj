(ns com.example.csv-service.data-test
  (:require [clojure.test :refer [deftest is are]]
            [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as gen]
            [com.example.csv-service.data :as data]))

;; Test needed?
(deftest test-parse-date
  (are [x y] (= (data/parse-date x) y)
    "01/01/1900" #inst "1900-01-01T00:00:00"
    "02/22/2000" #inst "2000-02-22T00:00:00"
    "03/03/3000" #inst "3000-03-03T00:00:00"

    ""           :clojure.spec.alpha/invalid
    "asdf"       :clojure.spec.alpha/invalid
    "1900/01/01" :clojure.spec.alpha/invalid
    "01/32/2000" :clojure.spec.alpha/invalid
    "00/01/2000" :clojure.spec.alpha/invalid
    "01-01-2000" :clojure.spec.alpha/invalid))

(deftest test-unparse-date
  (are [x y] (= (data/unparse-date x) y)
    #inst "1900-01-01" "01/01/1900"
    #inst "1900-02-10" "02/10/1900"))

(deftest test-conform-date
  (are [x y] (= (spec/conform ::data/date x) y)
    "01/01/1900" #inst "1900-01-01T00:00:00"
    "02/22/2000" #inst "2000-02-22T00:00:00"
    "03/03/3000" #inst "3000-03-03T00:00:00"

    ""           :clojure.spec.alpha/invalid
    "asdf"       :clojure.spec.alpha/invalid
    "1900/01/01" :clojure.spec.alpha/invalid
    "01/32/2000" :clojure.spec.alpha/invalid
    "00/01/2000" :clojure.spec.alpha/invalid
    "01-01-2000" :clojure.spec.alpha/invalid))

(deftest test-unform-date
  (are [x y] (= (spec/unform ::data/date x) y)
    #inst "1900-01-01T00:00:00" "01/01/1900"
    #inst "2000-02-22T00:00:00" "02/22/2000"
    #inst "3000-03-03T00:00:00" "03/03/3000"

    ;; Invalid unfroms should return invalid keywords?
    0    :clojure.spec.alpha/invalid
    5.0  :clojure.spec.alpha/invalid
    :foo :clojure.spec.alpha/invalid))
