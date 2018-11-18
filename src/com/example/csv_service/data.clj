(ns com.example.csv-service.data
  (:refer-clojure :exclude [read])
  (:require [clojure.spec.alpha :as spec]
            [clojure.string :refer [split join lower-case]]
            [clojure.java.io :as io])
  (:import [java.text SimpleDateFormat]
           [java.util TimeZone]))

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

(spec/def ::record
  (spec/cat :last-name string?
            :first-name string?
            :gender string?
            :favorite-color string?
            :date-of-birth ::date))

(spec/def ::csv-data
  (spec/cat :header (spec/coll-of string?)
            :data (spec/* (spec/spec ::record))))

(defn merge-data [base & others]
  (->> (mapcat :data others)
       (update base :data into)))

(defn from-lines [sep lines]
  (->> (map #(split % sep) lines)
       (spec/conform ::csv-data)))

(defn to-lines [sep data]
  (->> (spec/unform ::csv-data data)
       (map #(join sep %))))

;; Solution

(defn input-data []
  (with-open [r1 (io/reader "data/random_pipes.csv")
              r2 (io/reader "data/random_comma.csv")
              r3 (io/reader "data/random_space.csv")]
    (let [d1 (from-lines #" \| " (line-seq r1))
          d2 (from-lines #", " (line-seq r2))
          d3 (from-lines #" " (line-seq r3))]
      (merge-data d1 d2 d3))))

(defn write-lines [wtr lines]
  (doseq [line lines]
    (doto wtr
      (.write line)
      (.newLine))))

(defn solution-fn1 [data]
  (let [gord   {"Female" 0 "Male" 1}
        key-fn (juxt (comp gord :gender) :last-name)]
    (update data :data (partial sort-by key-fn))))

(defn solution-fn2 [data]
  (update data :data (partial sort-by :date-of-birth)))

(defn solution-fn3 [data]
  (update data :data (comp reverse (partial sort-by :last-name))))

(defn output-data [data]
  (with-open [w1 (io/writer "out/random_gender_last.csv")
              w2 (io/writer "out/random_dob.csv")
              w3 (io/writer "out/random_last.csv")]
    (->> (solution-fn1 data)
         (to-lines ",")
         (write-lines w1))
    (->> (solution-fn2 data)
         (to-lines ",")
         (write-lines w2))
    (->> (solution-fn3 data)
         (to-lines ",")
         (write-lines w3))))

(defn solution []
  (.mkdir (io/file "./out"))
  (output-data (input-data)))

(defn -main []
  (solution))

