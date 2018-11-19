(ns com.example.csv-service.data.spec
  (:require [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :refer [includes?]]
            [com.example.csv-service.data :as d])
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

(defn truncate-date [date]
  (-> (.toInstant date)
      (.truncatedTo ChronoUnit/DAYS)
      (Date/from)))

(defn gen-date []
  ;; Stop inst generator from making rediculous invalid dates.
  ;; e.g. #inst "549521-02-29T23:59:59.999-00:00"
  (->> (spec/inst-in #inst "0001-01-01" #inst "9999-12-31")
       (spec/gen)
       (gen/fmap truncate-date)))

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
  (spec/nilable (spec/coll-of ::record)))

(spec/def ::csv-data
  (spec/keys :req-un [::header]
             :opt-un [::data]))

;; fdef

(spec/fdef d/merge
  :args (spec/+ ::csv-data)
  :ret ::csv-data
  :fn (fn [{args :args ret :ret}]
        (= (set (mapcat :data args))
           (set (:data ret)))))

(defn ascending?
  [objs]
  (->> (partition 2 1 objs)
       (map #(apply compare %))
       (every? #(<= % 0))))

(defn descending?
  [objs]
  (->> (partition 2 1 objs)
       (map #(apply compare %))
       (every? #(>= % 0))))

(spec/def :sort-gender-lastname/ret
  (spec/and
   (spec/cat
    :female (spec/* (comp #{"Female"} :gender))
    :male (spec/* (comp #{"Male"} :gender)))
   (fn [{:keys [female male]}]
     (and (->> (map :last-name female)
               (ascending?))
          (->> (map :last-name male)
               (ascending?))))))

(spec/fdef d/sort-gender-lastname
  :args (spec/cat :data ::data)
  :ret :sort-gender-lastname/ret)

(spec/fdef d/sort-date-of-birth
  :args (spec/cat :data ::data)
  :ret #(->> (map :date-of-birth %)
             (ascending?)))

(spec/fdef d/sort-lastname
  :args (spec/cat :data ::data)
  :ret #(->> (map :last-name %)
             (descending?)))

