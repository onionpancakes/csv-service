(ns com.example.csv-service.server
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.interceptor :as i]
            [io.pedestal.interceptor.chain :refer [terminate]]
            [clojure.data.json :as json]
            [com.example.csv-service.part1 :as p1]
            [com.example.csv-service.data :as d]))

;; JSON

(defmulti json-str type)

(defmethod json-str java.util.Date
  [value]
  (d/unparse-date value))

(defmethod json-str :default
  [value]
  value)

(defn value-fn [_ v]
  (json-str v))

;; Handlers

(defn hello-world [req]
  {:status 200 :body "Hello World!"})

(def supported-seps
  {"pipe"  #" \| "
   "comma" #", "
   "space" #" "})

(def sep-error-response
  "Error response when sep query parameter is missing
  or invalid value."
  {:status  422
   :headers {"Content-Type" "application/json"}
   :body    (->> (str "Must include request parameter \"sep\" "
                      "of values \"pipe\", \"comma\", or \"space\".")
                 (array-map :error true :message)
                 (json/write-str))})

(def ensure-sep
  "Interceptor to ensure valid sep query parameter."
  {:name  ::ensure-sep
   :enter (fn [{{{sep :sep} :params} :request :as ctx}]
            (let [error? (not (supported-seps sep))]
              (cond-> ctx
                error? (assoc :response sep-error-response)
                error? (terminate))))})

(def read-error-response
  "Response for bad post data."
  {:status  422
   :headers {"Content-Type" "application/json"}
   :body    (->> {:error   true
                  :message "Data format error!"}
                 (json/write-str))})

(def read-post
  "Interceptor for reading post body. Conforms the body
  and puts it into the request map."
  {:name  ::read-post
   :enter (fn [{{{sep :sep} :params
                 body       :body} :request :as ctx}]
            (let [sep-re   (supported-seps sep)
                  csv-data (p1/read sep-re body)]
              (if (= csv-data :clojure.spec.alpha/invalid)
                (-> (assoc ctx :response read-error-response)
                    (terminate))
                (assoc-in ctx [:request ::csv-data] csv-data))))})

(defn post-handler
  "Handler for post data. Merges data into the state atom."
  [req]
  (swap! (::state req) d/merge (::csv-data req))
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (-> {:message "Posted input!"
                 :posted  (::csv-data req)}
                (json/write-str :value-fn value-fn))})

(def post-interceptors
  [ensure-sep read-post post-handler])

(defn gender-handler [{state ::state}]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (-> @state
                (update :data d/sort-gender-lastname)
                (json/write-str :value-fn value-fn))})

(defn dob-handler [{state ::state}]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (-> @state
                (update :data d/sort-date-of-birth)
                (json/write-str :value-fn value-fn))})

(defn name-handler [{state ::state}]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (-> @state
                (update :data d/sort-lastname)
                (json/write-str :value-fn value-fn))})

;; Server

(def routes
  (route/expand-routes
   #{["/hello" :get hello-world :route-name :hello]
     ["/records" :post post-interceptors :route-name :post]
     ["/records/gender" :get gender-handler :route-name :gender]
     ["/records/birthdate" :get dob-handler :route-name :birthdate]
     ["/records/name" :get name-handler :route-name :name]}))

(def service
  {::http/routes routes
   ::http/type   :jetty
   ::http/port   8080})

(def initial-state
  {:header ["LastName" "FirstName" "Gender" "FavoriteColor" "DateOfBirth"]})

(defn state-interceptor [state]
  {:name ::state
   :enter (fn [ctx]
            (assoc-in ctx [:request ::state] state))})

(defn interceptors [state]
  [(i/interceptor (state-interceptor state))])

(defn create-server [service state]
  (-> service
      (http/default-interceptors)
      (http/dev-interceptors)
      (update ::http/interceptors (partial into (interceptors state)))
      (http/create-server)))

(defonce state
  (atom initial-state))

(defonce server
  (create-server service state))

(defn start []
  (http/start server))

(defn stop []
  (http/stop server))

(defn -main []
  (start))

