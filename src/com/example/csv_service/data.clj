(ns com.example.csv-service.data
  (:refer-clojure :exclude [read])
  (:require [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as gen]
            [clojure.java.io :as io])
  (:import [java.text SimpleDateFormat]
           [java.util TimeZone]))

(def date-format
  (doto (SimpleDateFormat. "MM/dd/yyyy")
    (.setTimeZone (TimeZone/getTimeZone "UTC"))))

(defn parse-date
  [s]
  (.parse date-format s))

(defn unparse-date
  [d]
  (.format date-format d))

(spec/def ::date-of-birth
  (spec/with-gen
    (spec/and
     string?
     (spec/conformer parse-date unparse-date))
    #(gen/fmap unparse-date (spec/gen inst?))))

(spec/def ::record
  (spec/cat :last-name string?
            :first-name string?
            :gender string?
            :favorite-color string?
            :date-of-birth ::date-of-birth))

(spec/def ::header
  #{["LastName" "FirstName" "Gender"
     "FavoriteColor" "DateOfBirth"]})

(spec/def ::csv-data
  (spec/cat :header ::header
            :data (spec/* (spec/spec ::record))))

(defn parse-record
  [rec]
  (spec/conform ::record rec))

(defn parse [data]
  (spec/assert (first data) ::header)
  (->> (drop 1 data)
       (map parse-record)))

(defn read [rdr sep]
  (->> (line-seq rdr)
       (map #(clojure.string/split % sep))
       (parse)))

