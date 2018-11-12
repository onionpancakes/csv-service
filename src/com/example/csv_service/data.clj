(ns com.example.csv-service.data
  (:refer-clojure :exclude [read])
  (:require [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :refer [split join includes?]])
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
  (let [cf (spec/conformer parse-date unparse-date)]
    (spec/with-gen (spec/and string? cf)
      #(gen/fmap unparse-date (spec/gen inst?)))))

(spec/def ::header
  #{["LastName" "FirstName" "Gender"
     "FavoriteColor" "DateOfBirth"]})

(def ^:dynamic *separators*
  #{" | ", ", ", " "})

(defn includes-separators?
  [s]
  (->> *separators*
       (some (partial includes? s))
       (boolean)))

(spec/def ::string
  (spec/and string? (complement includes-separators?)))

(spec/def ::record
  (spec/cat :last-name ::string
            :first-name ::string
            :gender ::string
            :favorite-color ::string
            :date-of-birth ::date))

(spec/def ::csv-data
  (spec/cat :header ::header
            :data (spec/* (spec/spec ::record))))

(defn read [rdr sep]
  (let [lines (->> (line-seq rdr)
                   (map #(split % sep)))]
    (if-not (spec/valid? ::csv-data lines)
      (let [s (spec/explain-str ::csv-data lines)
            d (spec/explain-data ::csv-data lines)]
        (throw (ex-info s d))))
    (spec/conform ::csv-data lines)))

