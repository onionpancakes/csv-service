(ns com.example.csv-service.part1
  (:refer-clojure :exclude [read])
  (:require [clojure.java.io :as io]
            [clojure.string :refer [split join]]
            [clojure.spec.alpha :as spec]
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

(defn -main []
  (solution input-files)
  (println "Files parsed into ./out directory."))

