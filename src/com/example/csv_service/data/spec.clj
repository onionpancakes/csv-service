(ns com.example.csv-service.data.spec
  (:require [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :refer [includes?]])
  (:import [java.util Date]
           [java.time.temporal ChronoUnit]))

;; String util

(def ^:dynamic *separators*
  #{" | ", ", ", " "})

(defn includes-separators?
  [s]
  (->> *separators*
       (some (partial includes? s))
       (boolean)))

(defn gen-str-no-sep []
  (->> (spec/gen string?)
       (gen/such-that (complement includes-separators?))))

(spec/def ::string
  (spec/with-gen string? gen-str-no-sep))

;; Date util

(defn gen-date* [[date plus]]
  (-> (.toInstant date)
      (.plusSeconds plus)
      (.truncatedTo ChronoUnit/DAYS)
      (Date/from)))

(defn gen-date []
  (->> (gen/tuple (spec/gen inst?) (gen/choose 0 1000000000))
       (gen/fmap gen-date*)))

(spec/def ::date
  (spec/with-gen inst? gen-date))

;; Data Spec

(def header
  ["LastName" "FirstName" "Gender" "FavoriteColor" "DateOfBirth"])

(def genders
  #{"Female" "Male"})

(spec/def ::header
  #{header})

(spec/def ::last-name ::string)

(spec/def ::first-name ::string)

(spec/def ::gender genders)

(spec/def ::favorite-color ::string)

(spec/def ::date-of-birth ::date)

(spec/def ::record
  (spec/keys :req-un [::last-name ::first-name ::gender
                      ::favorite-color ::date-of-birth]))

(spec/def ::data
  (spec/coll-of ::record))

(spec/def ::csv-data
  (spec/keys :req-un [::header ::data]))

