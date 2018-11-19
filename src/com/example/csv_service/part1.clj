(ns com.example.csv-service.part1
  (:refer-clojure :exclude [read])
  (:require [clojure.java.io :as io]
            [clojure.string :refer [split join]]
            [clojure.spec.alpha :as spec]
            [clojure.pprint :refer [print-table]]
            [com.example.csv-service.data :as d]))

(defn read [sep f]
  (with-open [rdr (io/reader f)]
    (->> (line-seq rdr)
         (map #(split % sep))
         (spec/conform ::d/lines))))

(defn write-lines [wtr lines]
  (doseq [line lines]
    (doto wtr
      (.write line)
      (.newLine))))

(defn write [sep f data]
  (with-open [wtr (io/writer f)]
    (->> (spec/unform ::d/lines data)
         (map (partial join sep))
         (write-lines wtr))))

(def input-files
  [{:filename "data/random_pipes.csv"
    :sep #" \| "}
   {:filename "data/random_comma.csv"
    :sep #", "}
   {:filename "data/random_space.csv"
    :sep #" "}])

(defn solution [files]
  (.mkdir (io/file "./out"))
  (let [csv-data (->> files
                      (map (juxt :sep :filename))
                      (map (partial apply read))
                      (apply d/merge))]
    (->> (update csv-data :data d/sort-gender-lastname)
         (write "," "out/sorted_gender_lastname.csv"))
    (->> (update csv-data :data d/sort-date-of-birth)
         (write "," "out/sorted_date_of_birth.csv"))
    (->> (update csv-data :data d/sort-lastname)
         (write "," "out/sorted_lastname.csv"))))

(def screen-keys
  [:last-name :first-name :gender :favorite-color :date-of-birth])

(defn solution-screen
  "Prints to terminal instead of writing to files."
  [files]
  (let [csv-data (->> files
                      (map (juxt :sep :filename))
                      (map (partial apply read))
                      (apply d/merge))]

    (println "Sorted by gender and lastname (ascending):")
    (->> (:data csv-data)
         (d/sort-gender-lastname)
         (map #(update % :date-of-birth d/unparse-date))
         (print-table screen-keys))

    (println)
    (println "Sorted by date of birth (ascending):")
    (->> (:data csv-data)
         (d/sort-date-of-birth)
         (map #(update % :date-of-birth d/unparse-date))
         (print-table screen-keys))

    (println)
    (println "Sorted by lastname (descending):")
    (->> (:data csv-data)
         (d/sort-lastname)
         (map #(update % :date-of-birth d/unparse-date))
         (print-table screen-keys))))

(defn -main []
  (solution-screen input-files))

