(ns com.example.csv-service.data-test
  (:require [clojure.test :refer [deftest is are]]
            [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as gen]
            [com.example.csv-service.data :as data]
            [com.example.csv-service.data.spec :as data.spec]))

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
    #inst "3000-03-03T00:00:00" "03/03/3000"))

(def header data.spec/header)

(deftest test-csv-data-spec
  (are [x y] (= (spec/valid? ::data/csv-data x) y)
    [header]                               true
    [header
     ["" "" "Female" "" "01/01/2008"]]     true
    [header
     ["a" "" "Male" "" "01/01/2008"]]      true
    [header
     ["a" "" "Male" "" "01/01/2008"]
     ["a" "" "Female" "bar" "01/30/2018"]] true
    []                                     false
    [["a" "b" "" "" "01/30/2018"]]         false
    [header
     ["a" "b" "c" "" "01/01/2018"]]        false
    [header
     ["a" "b" "" "" "01/32/2018"]]         false))
