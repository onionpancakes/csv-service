(ns user
  (:require [clojure.repl :refer :all]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.spec.alpha :as spec]
            [clojure.java.io :as io]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest]
            [io.pedestal.http :as http]
            [com.example.csv-service.data :as data]
            [com.example.csv-service.data.spec :as data.spec]
            [com.example.csv-service.server :as server]
            [com.example.csv-service.test :refer [run-tests]]
            [com.example.csv-service.data-test :as data-test]
            [com.example.csv-service.part1 :as part1]))

(alias 'stc 'clojure.spec.test.check)

(defn gen-data []
  (gen/generate (spec/gen ::data.spec/csv-data)))

(defn gen-random-sample [dir]
  (.mkdir (io/file (str "./" dir)))
  (->> (gen-data)
       (part1/write " | " (str dir "/random_pipes.csv")))
  (->> (gen-data)
       (part1/write ", " (str dir "/random_comma.csv")))
  (->> (gen-data)
       (part1/write " " (str dir "/random_space.csv"))))

(defonce server
  (->> {:env          :dev
        ::http/join?  false
        ::http/routes #(deref #'server/routes)}
       (merge server/service)
       (http/default-interceptors)
       (http/dev-interceptors)
       (http/create-server)))

(defn start []
  (http/start server))

(comment
  (with-open [rdr (io/reader "data/sample_pipes.csv")]
    (->> (data/read rdr #" \| ")
         (doall))))
