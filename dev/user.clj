(ns user
  (:require [clojure.repl :refer :all]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.spec.alpha :as spec]
            [clojure.java.io :as io]
            [clojure.spec.gen.alpha :as gen]
            [io.pedestal.http :as http]
            [com.example.csv-service.data :as data]
            [com.example.csv-service.data.spec :as data.spec]
            [com.example.csv-service.server :as server]
            [com.example.csv-service.test :refer [run-tests]]))

(defn gen-data []
  (gen/generate (spec/gen ::data.spec/csv-data)))

(defn gen-random-sample []
  (with-open [w1 (io/writer "data/random_pipes.csv")
              w2 (io/writer "data/random_comma.csv")
              w3 (io/writer "data/random_space.csv")]
    (->> (gen-data)
         (data/to-lines " | ")
         (data/write-lines w1))
    (->> (gen-data)
         (data/to-lines ", ")
         (data/write-lines w2))
    (->> (gen-data)
         (data/to-lines " ")
         (data/write-lines w3))))

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
