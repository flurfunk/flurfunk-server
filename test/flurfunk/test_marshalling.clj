(ns flurfunk.test-marshalling
  (:use clojure.test
        flurfunk.marshalling))

(deftest test-marshal-message
  (is (= "<message id='1' author='felix'>foobar</message>"
         (marshal-message {:id "1" :author "felix" :body "foobar"}))))

(deftest test-marshal-messages
  (is (= "<messages><message id='1' author='felix'>foobar</message></messages>"
         (marshal-messages [{:id "1" :author "felix" :body "foobar"}]))))

(deftest test-unmarshal-message
  (is (= {:id "1" :author "felix" :body "foobar"}
         (unmarshal-message {:attrs {:id "1" :author "felix"}
                             :content ["foobar"]}))))

(deftest test-unmarshal-message-without-id
  (is (= {:author "felix" :body "foobar"}
         (unmarshal-message {:attrs {:author "felix"}
                             :content ["foobar"]}))))

(deftest test-unmarshal-messages
  (is (= [{:id "1" :author "felix" :body "foobar"}]
         (unmarshal-messages {:content [{:attrs {:id "1" :author "felix"}
                                         :content ["foobar"]}]}))))