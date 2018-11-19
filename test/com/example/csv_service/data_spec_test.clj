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

(deftest test-ascending?
  (are [x y] (= (data.spec/ascending? x) y)
    []                          true
    [""]                        true
    ["" ""]                     true
    ["a" "b"]                   true
    ["a" "a" "b"]               true
    [#inst "2000" #inst "2001"] true
    ["b" "a"]                   false
    ["b" "a" "b"]               false
    [#inst "2001" #inst "2000"] false))

(deftest test-descending?
  (are [x y] (= (data.spec/descending? x) y)
    []                          true
    [""]                        true
    ["" ""]                     true
    ["b" "a"]                   true
    ["b" "a" "a"]               true
    [#inst "2001" #inst "2000"] true
    ["a" "b"]                   false
    ["b" "a" "b"]               false
    [#inst "2000" #inst "2001"] false))

