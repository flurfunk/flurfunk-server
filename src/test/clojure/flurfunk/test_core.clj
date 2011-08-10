(ns flurfunk.test-core
  (:use clojure.test
        clojure.contrib.string
        flurfunk.core)
  (:require [clojure.xml :as xml]
            [clojure.contrib.duck-streams :as streams]
            [clojure.contrib.io :as io]))

(defn http-get [resource & params]
  (main-routes {:request-method :get :uri resource :params (first params)}))

(defn http-get-string [resource]
  (:body (http-get resource)))

(defn http-get-xml [resource]
  (xml/parse (io/input-stream (streams/to-byte-array
                               (http-get-string resource)))))

(defn parse-message [xml]
  (let [attrs (:attrs xml)]
    {:body (first (:content xml))
     :id (:id attrs)
     :author (:author attrs)}))

(defn parse-messages [xml]
  (map (fn [x] (parse-message x))
       (:content xml)))

(deftest test-index-page
  (is (substring? "flurfunk.js"
                  (http-get-string "/"))))

(deftest test-get-messages-successful
  (is (= 200 (:status (http-get "/messages")))))

(deftest test-get-messages-empty
  (is (empty? (parse-messages (http-get-xml "/messages")))))

(defn messages-fixture [f]
  (binding [get-messages (fn [] [{:body "foo" :id "1337" :author "thomas"}
                                 {:body "" :id "2448" :author "felix"}])]
    (f)))

(deftest test-get-messages-body
  (messages-fixture
   (fn []
     (is (= "foo"
            (:body (first (parse-messages
                           (http-get-xml "/messages")))))))))

(deftest test-get-messages-attributes
  (messages-fixture
   (fn []
     (let [messages (parse-messages (http-get-xml "/messages"))
           message (first messages)]
       (are (= "1337" (:id message))
            (= "thomas" (:author message)))))))

(deftest test-get-message-not-found
  (is (= 404
         (:status (http-get "/message/1337")))))

(deftest test-get-message-empty
  (messages-fixture
   (fn []
     (is (empty? 
          (:body (parse-message (http-get-xml "/message/2448"))))))))

(deftest test-get-message-body
  (messages-fixture
   (fn []
     (is (= "foo"
            (:body (parse-message (http-get-xml "/message/1337"))))))))

(deftest test-get-message-attributes
  (messages-fixture
   (fn []
     (let [message (parse-message (http-get-xml "/message/1337"))]
           (are (= "1337" (:id message))
                (= "thomas" (:author message)))))))
