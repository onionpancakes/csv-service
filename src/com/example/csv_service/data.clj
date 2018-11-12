(ns com.example.csv-service.data
  (:refer-clojure :exclude [read])
  (:require [clojure.spec.alpha :as spec]
            [clojure.string :refer [split join lower-case]]
            [clojure.java.io :as io]
            [com.example.csv-service.data.spec :as data.spec])
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
  (spec/cat :last-name ::data.spec/string
            :first-name ::data.spec/string
            :gender ::data.spec/gender
            :favorite-color ::data.spec/string
            :date-of-birth ::date))

(spec/def ::csv-data
  (spec/cat :header ::data.spec/header
            :data (spec/* (spec/spec ::record))))

(defn read [rdr sep]
  (let [lines (->> (line-seq rdr)
                   (map #(split % sep)))]
    (if-not (spec/valid? ::csv-data lines)
      (let [s (spec/explain-str ::csv-data lines)
            d (spec/explain-data ::csv-data lines)]
        (throw (ex-info s d))))
    (spec/conform ::csv-data lines)))

(defn write [wtr sep data]
  (doseq [line (spec/unform ::csv-data data)]
    (doto wtr
      (.write (join sep line))
      (.newLine))))

(defn merge-data [base & others]
  (->> (mapcat :data others)
       (update base :data into)))

(def gender-order
  {"female" 0
   "Female" 1
   "male"   2
   "Male"   3})

(defn -main []
  (.mkdir (io/file "./out"))
  (with-open [r1 (io/reader "data/random_pipes.csv")
              r2 (io/reader "data/random_comma.csv")
              r3 (io/reader "data/random_space.csv")
              w1 (io/writer "out/random_gender_last.csv")
              w2 (io/writer "out/random_dob.csv")
              w3 (io/writer "out/random_last.csv")]
    (let [m (merge-data (read r1 #" \| ")
                        (read r2 #", ")
                        (read r3 #" "))]
      (->> (partial sort-by (juxt (comp gender-order
                                        lower-case
                                        :gender)
                                  :last-name))
           (update m :data)
           (write w1 ","))
      (->> (partial sort-by :date-of-birth)
           (update m :data)
           (write w2 ","))
      (->> (partial sort-by :last-name)
           (comp reverse)
           (update m :data)
           (write w3 ",")))))

