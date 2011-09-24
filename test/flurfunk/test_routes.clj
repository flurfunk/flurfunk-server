(ns flurfunk.test-routes
  (:use clojure.test
        flurfunk.routes)
  (:require [clojure.xml :as xml]
            [clojure.contrib.duck-streams :as streams]
            [clojure.contrib.io :as io]
            [clojure.contrib.string :as string]
            [flurfunk.marshalling :as ms]
            [flurfunk.storage :as storage]))

(defn http-get [resource & params]
  (main-routes {:request-method :get :uri resource :params (first params)}))

(defn http-get-string [resource & params]
  (:body (apply http-get resource params)))

(defn http-get-xml [resource & params]
  (xml/parse (io/input-stream (streams/to-byte-array
                               (apply http-get-string resource params)))))

(deftest test-index-page
  (let [response (http-get-string "/")]
    (are [s] (string/substring? s response)
         "flurfunk.js"
         "flurfunk.css")))
         
(deftest test-get-messages-successful
  (is (= 200 (:status (http-get "/messages")))))

(deftest test-get-messages-empty
  (binding [storage/get-messages (fn [] [])]
    (is (empty? (ms/unmarshal-messages (http-get-xml "/messages"))))))

(deftest test-get-messages
  (binding [storage/get-messages
            (fn [] [{:body "foo" :id "1337" :author "thomas" :timestamp 10001}
                    {:body "" :id "2448" :author "felix" :timestamp 10002}])]
    (let [messages (ms/unmarshal-messages (http-get-xml "/messages"))
          message (first messages)]
      (are [v k] (= v (k message))
           "foo" :body
           "1337" :id
           "thomas" :author
           10001 :timestamp))))

(deftest test-get-messages-since
  (binding [storage/get-messages
            (fn ([])
              ([since] (if (= since 1000000000000)
                         [{:body "foo" :id "1337" :author "thomas"
                           :timestamp 1000000000001}])))]
    (let [messages (ms/unmarshal-messages
                    (http-get-xml "/messages" {:since "1000000000000"}))
          message (first messages)]
      (is (= (count messages) 1))
      (are [v k] (= v (k message))
           "foo" :body
           "1337" :id
           "thomas" :author
           1000000000001 :timestamp))))

(deftest test-get-message-not-found
  (binding [storage/find-message (fn [id] nil)]
    (is (= 404
           (:status (http-get "/message/1337"))))))

(deftest test-get-message-empty
  (binding [storage/find-message (fn [id] {:body ""})]
    (is (empty? 
         (:body (ms/unmarshal-message (http-get-xml "/message/2448")))))))

(deftest test-get-message
  (binding [storage/find-message
            (fn [id] {:id "1337" :author "thomas" :timestamp 10001 :body "foo"})]
    (let [message (ms/unmarshal-message (http-get-xml "/message/1337"))]
      (are [v k] (= v (k message))
           "foo" :body
           "1337" :id
           "thomas" :author
           10001 :timestamp))))

(deftest test-post-message-successful
  (binding [storage/add-message (fn [message])]
    (is (= 200
           (:status (main-routes {:request-method :post :uri "/message"
                                  :body "<message></message>"}))))))

(deftest test-post-message
  (let [messages (transient [])]
    (binding [storage/add-message (fn [message] (conj! messages message))]
      (main-routes {:request-method :post :uri "/message"
                    :body "<message author='Felix'>foobar</message>"})
      (let [message (first (persistent! messages))]
        (are [v k] (= v (k message))
             "foobar" :body
             "Felix" :author)
        (is (> (:timestamp message) 0))))))
