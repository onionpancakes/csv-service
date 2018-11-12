(ns com.example.csv-service.data-spec-test
  (:require [clojure.test :refer [deftest are]]
            [clojure.spec.alpha :as spec]
            [com.example.csv-service.data.spec :as data.spec]))

(deftest test-includes-separators?
  (are [x y] (= (data.spec/includes-separators? x) y)
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
  (are [x y] (binding [data.spec/*separators* ["," "."]]
               (= (data.spec/includes-separators? x) y))
    "."         true
    ","         true
    "foo,bar"   true
    "foo.bar"   true
    "foo"       false
    ""          false
    "foo | bar" false))

(deftest test-string-spec
  (are [x y] (= (spec/valid? ::data.spec/string x) y)
    "foo"       true
    ""          true
    "foo,bar"   true
    "foo.bar"   true
    "foo bar"   false
    "foo, bar"  false
    "foo | bar" false))

