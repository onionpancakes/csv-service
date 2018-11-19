(ns com.example.csv-service.server-test
  (:require [clojure.test :refer [deftest is are use-fixtures]]
            [clojure.data.json :as json]
            [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest]
            [io.pedestal.http :as http]
            [clj-http.client :as client]
            [com.example.csv-service.server :as serv]
            [com.example.csv-service.part1 :as p1]
            [com.example.csv-service.data :as d]
            [com.example.csv-service.data.spec :as ds])
  (:import [java.io StringWriter]))

(defonce state
  (atom serv/initial-state))

(defonce server
  (-> {:env          :dev
       ::http/join?  false
       ::http/routes #(deref #'serv/routes)
       ::http/port   9000}
      (as-> x (merge serv/service x))
      (serv/create-server state)
      (http/start)))

;; HTTP

(defn get-hello []
  (slurp "http://localhost:9000/hello"))

(def supported-seps
  {"pipe"  " | "
   "comma" ", "
   "space" " "})

(defn post [sep csv-data]
  (let [sw   (StringWriter.)
        _    (p1/write (get supported-seps sep ",") sw csv-data)
        body (str sw)]
    (client/post "http://localhost:9000/records"
                 {:body             body
                  :query-params     {:sep sep}
                  :throw-exceptions false})))

(defn get-gender []
  (client/get "http://localhost:9000/records/gender"))

(defn get-dob []
  (client/get "http://localhost:9000/records/birthdate"))

(defn get-name []
  (client/get "http://localhost:9000/records/name"))

;; Helpers

(defn conform-body [body]
  (let [res (json/read-str body :key-fn keyword)]
    (->> (:data res)
         (map (juxt :last-name :first-name :gender
                    :favorite-color :date-of-birth))
         (cons (:header res))
         (spec/conform ::d/lines))))

;; Data

(def data-empty
  {:header ds/header})

(def data-random
  (-> (spec/and ::ds/csv-data
                ;; Not empty data
                (comp seq :data))
      (spec/gen)
      (gen/generate)))

;; Fixtures

(defn clear-data [f]
  (reset! state data-empty)
  (f))

(use-fixtures :each clear-data)

;; Tests

(deftest test-hello
  (is (= (get-hello) "Hello World!")))

(deftest test-content-type
  (are [x y] (= (get (:headers x) "Content-Type") y)
    (post "foo" nil)           "application/json"
    (post "pipe" nil)          "application/json"
    (post "comma" nil)         "application/json"
    (post "space" nil)         "application/json"
    (post "pipe" data-empty)   "application/json"
    (post "comma" data-empty)  "application/json"
    (post "space" data-empty)  "application/json"
    (post "pipe" data-random)  "application/json"
    (post "comma" data-random) "application/json"
    (post "space" data-random) "application/json"
    (get-gender)               "application/json"
    (get-dob)                  "application/json"
    (get-name)                 "application/json"))

(deftest test-status
  (are [x y] (= (:status x) y)
    (post "foo" nil)           422
    (post "pipe" nil)          422
    (post "comma" nil)         422
    (post "space" nil)         422
    (post "pipe" data-empty)   200
    (post "comma" data-empty)  200
    (post "space" data-empty)  200
    (post "pipe" data-random)  200
    (post "comma" data-random) 200
    (post "space" data-random) 200
    (get-gender)               200
    (get-dob)                  200
    (get-name)                 200))

(deftest test-post
  (reset! state data-empty)
  (let [in data-random
        _  (post "pipe" in)
        r1 (get-gender)
        r2 (get-dob)
        r3 (get-name)]
    (is (= (set (:data in))
           (set (-> r1 :body conform-body :data))
           (set (-> r2 :body conform-body :data))
           (set (-> r3 :body conform-body :data))))))

;; Property tests

;; Note: Property tests seems to run concurrently.
;; Due to shared atom state, locking is nessessary.
;; TODO: figure out lockless tests?

(defn property-identity
  [sep csv-data]
  (locking state
    (reset! state data-empty)
    (post sep csv-data)
    [(-> (get-gender) :body conform-body)
     (-> (get-dob) :body conform-body)
     (-> (get-name) :body conform-body)]))

(spec/fdef property-identity
  :args (spec/cat :sep (set (keys supported-seps))
                  :csv-data ::ds/csv-data)
  :ret  (spec/coll-of ::ds/csv-data)
  :fn (fn [{{in :csv-data} :args ret :ret}]
        (->> (map :data ret)
             (map set)
             (apply = (set (:data in))))))

(defn property-sorted-gender
  [sep init-st to-post]
  (locking state
    (reset! state init-st)
    (doseq [csv-data to-post]
      (post sep csv-data))
    (-> (get-gender) :body conform-body :data)))

(spec/fdef property-sorted-gender
  :args (spec/cat :sep (set (keys supported-seps))
                  :init-st ::ds/csv-data
                  :to-post (spec/coll-of ::ds/csv-data :gen-max 8))
  :ret (spec/cat
        :female (spec/* (comp #{"Female"} :gender))
        :male (spec/* (comp #{"Male"} :gender))))

(defn property-sorted-dob
  [sep init-st to-post]
  (locking state
    (reset! state init-st)
    (doseq [csv-data to-post]
      (post sep csv-data))
    (->> (get-dob)
         (:body)
         (conform-body)
         (:data)
         (map :date-of-birth))))

(spec/fdef property-sorted-dob
  :args (spec/cat :sep (set (keys supported-seps))
                  :init-st ::ds/csv-data
                  :to-post (spec/coll-of ::ds/csv-data :gen-max 8))
  :ret ds/ascending?)

(defn property-sorted-name
  [sep init-st to-post]
  (locking state
    (reset! state init-st)
    (doseq [csv-data to-post]
      (post sep csv-data))
    (->> (get-name)
         (:body)
         (conform-body)
         (:data)
         (map :last-name))))

(spec/fdef property-sorted-name
  :args (spec/cat :sep (set (keys supported-seps))
                  :init-st ::ds/csv-data
                  :to-post (spec/coll-of ::ds/csv-data :gen-max 8))
  :ret ds/descending?)

;; Test specced fns

(alias 'stc 'clojure.spec.test.check)

(def check-fns
  [`property-identity
   `property-sorted-gender
   `property-sorted-dob
   `property-sorted-name])

(def check-opts
  {::stc/opts {:num-tests 40}})

(deftest test-specced-fns
  (-> (stest/check check-fns check-opts)
      (stest/summarize-results)
      (as-> x
        (= (:total x) (:check-passed x)))
      (is)))

