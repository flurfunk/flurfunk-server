(ns flurfunk.test-core
  (:use clojure.test
        clojure.contrib.string ;; TODO: Use require, if possible
        flurfunk.core)
  (:require [clojure.xml :as xml]
            [clojure.contrib.duck-streams :as streams]
            [clojure.contrib.io :as io]
            [flurfunk.marshalling :as ms]
            [flurfunk.storage :as storage]))

(defn http-get [resource & params]
  (main-routes {:request-method :get :uri resource :params (first params)}))

(defn http-get-string [resource]
  (:body (http-get resource)))

(defn http-get-xml [resource]
  (xml/parse (io/input-stream (streams/to-byte-array
                               (http-get-string resource)))))

(deftest test-index-page
  (is (substring? "flurfunk.js"
                  (http-get-string "/"))))

(deftest test-get-messages-successful
  (is (= 200 (:status (http-get "/messages")))))

(deftest test-get-messages-empty
  (binding [storage/get-messages (fn [] [])]
    (is (empty? (ms/unmarshal-messages (http-get-xml "/messages"))))))

(deftest test-get-messages
  (binding [storage/get-messages
            (fn [] [{:body "foo" :id "1337" :author "thomas"}
                    {:body "" :id "2448" :author "felix"}])]
    (let [messages (ms/unmarshal-messages (http-get-xml "/messages"))
          message (first messages)]
      (are [v k] (= v (k message))
           "foo" :body
           "1337" :id
           "thomas" :author))))

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
            (fn [id] {:id "1337" :author "thomas" :body "foo"})]
    (let [message (ms/unmarshal-message (http-get-xml "/message/1337"))]
      (are [v k] (= v (k message))
           "foo" :body
           "1337" :id
           "thomas" :author))))

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
             "Felix" :author)))))
