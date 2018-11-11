(ns com.example.csv-service.data
  (:refer-clojure :exclude [read])
  (:require [clojure.spec.alpha :as spec]
            [clojure.java.io :as io]))

(spec/def ::record
  (spec/cat :last-name string?
            :first-name string?
            :gender string?
            :favorite-color string?
            :date-of-birth string?))

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

