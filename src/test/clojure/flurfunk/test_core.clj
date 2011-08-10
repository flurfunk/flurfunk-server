(ns flurfunk.test-core
  (:use clojure.test
        clojure.contrib.string
        flurfunk.core)
  (:require [clojure.xml :as xml]
            [clojure.contrib.duck-streams :as streams]
            [clojure.contrib.io :as io]))

(defn request [resource & params]
  (main-routes {:request-method :get :uri resource :params (first params)}))

(defn request-body [resource]
  (:body (request resource)))

(defn parsed-request-body [resource]
  (xml/parse (io/input-stream (streams/to-byte-array
                               (request-body resource)))))

(defn parse-messages [xml]
  (map (fn [x]
         (let [attrs (:attrs x)]
           {:body (first (:content x))
            :id (:id attrs)
            :author (:author attrs)}))
       (:content xml)))

(deftest test-index-page
  (is (substring? "flurfunk.js"
                  (request-body "/"))))

(deftest test-get-messages-successful
  (is (= 200 (:status (request "/messages")))))

(deftest test-get-messages-empty
  (is (empty? (parse-messages (parsed-request-body "/messages")))))

(deftest test-get-messages-body
  (binding [get-messages (fn [] [{:body "foo" :id "1337"}])]
    (is (= "foo"
           (:body (first (parse-messages
                          (parsed-request-body "/messages"))))))))

(deftest test-get-messages-attributes
  (binding [get-messages (fn [] [{:body "foo" :id "1337" :author "thomas"}])]
    (let [messages (parse-messages (parsed-request-body "/messages"))
          message (first messages)]
      (are (= "1337" (:id message))
           (= "thomas" (:author message))))))
