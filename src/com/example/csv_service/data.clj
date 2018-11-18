(ns com.example.csv-service.data
  (:refer-clojure :exclude [merge])
  (:require [clojure.spec.alpha :as spec])
  (:import [java.text SimpleDateFormat]
           [java.util TimeZone]))

;; Parse

(def date-format
  (doto (SimpleDateFormat. "MM/dd/yyyy")
    (.setTimeZone (TimeZone/getTimeZone "UTC"))
    (.setLenient false)))

(defn parse-date
  [s]
  (try
    (.parse date-format s)
    (catch Exception _
      :clojure.spec.alpha/invalid)))

(defn unparse-date
  [d]
  (.format date-format d))

(spec/def ::date
  (spec/conformer parse-date unparse-date))

(spec/def ::line
  (spec/cat :last-name string?
            :first-name string?
            :gender string?
            :favorite-color string?
            :date-of-birth ::date))

(spec/def ::lines
  (spec/cat :header (spec/coll-of string?)
            :data (spec/* (spec/spec ::line))))

;; Merge

(defn merge [base & others]
  (->> (mapcat :data others)
       (update base :data into)))

;; Sort fns

(def gender-order
  {"Female" 0 "Male" 1})

(def sort-gender-lastname-keyfn
  (juxt (comp gender-order :gender) :last-name))

(defn sort-gender-lastname
  [data]
  (sort-by sort-gender-lastname-keyfn data))

(defn sort-date-of-birth
  [data]
  (sort-by :date-of-birth data))

(defn sort-lastname
  [data]
  (reverse (sort-by :last-name data)))

