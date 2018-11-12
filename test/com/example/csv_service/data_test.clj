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
    #inst "3000-03-03T00:00:00" "03/03/3000"))

(deftest test-includes-separators?
  (are [x y] (= (data/includes-separators? x) y)
    " "         true
    ", "        true
    " | "       true
    "foo bar"   true
    "foo, bar"  true
    "foo | bar" true
    " foo"      true
    ""          false
    "foo"       false
    "foo,bar"   false
    "foo|bar"   false))

(deftest test-includes-separators?-binding
  (are [x y] (binding [data/*separators* ["," "."]]
               (= (data/includes-separators? x) y))
    "."         true
    ","         true
    "foo,bar"   true
    "foo.bar"   true
    "foo"       false
    ""          false
    "foo | bar" false))

(deftest test-string-spec
  (are [x y] (= (spec/valid? ::data/string x) y)
    "foo"       true
    ""          true
    "foo,bar"   true
    "foo.bar"   true
    "foo bar"   false
    "foo, bar"  false
    "foo | bar" false))

