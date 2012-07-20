(ns flurfunk.server.test-marshalling
  (:use clojure.test
        flurfunk.server.marshalling))

(deftest test-marshal-message
  (is (= "<message id='1' author='felix' timestamp='1337'>foobar</message>"
         (marshal-message {:id "1" :author "felix" :timestamp 1337
                           :body "foobar"}))))

(deftest test-marshal-messages
  (is (= (str "<messages>"
              "<message id='1' author='felix' timestamp='1337'>foobar</message>"
              "</messages>")
         (marshal-messages [{:id "1" :author "felix" :timestamp 1337
                             :body "foobar"}]))))

(deftest test-marshal-message-with-channels
  (is (= "<message channels='Users,Important'></message>"
         (marshal-message {:channels "Users,Important"}))))

(deftest test-unmarshal-message
  (is (= {:id "1" :author "felix" :timestamp 1000000000001 :body "foobar"}
         (unmarshal-message {:attrs {:id "1" :author "felix"
                                     :timestamp "1000000000001"}
                             :content ["foobar"]}))))

(deftest test-unmarshal-message-without-id-and-timestamp
  (is (= {:author "felix" :body "foobar"}
         (unmarshal-message {:attrs {:author "felix"}
                             :content ["foobar"]}))))

(deftest test-unmarshal-message-with-html
  (is (= {:author "felix" :timestamp 1337 :body "foo&lt;br/&gt;bar"}
         (unmarshal-message {:attrs {:author "felix" :timestamp "1337"}
                             :content ["foo<br/>bar"]}))))

(deftest test-unmarshal-messages
  (is (= [{:id "1" :author "felix" :timestamp 1337 :body "foobar"}]
           (unmarshal-messages {:content [{:attrs {:id "1" :author "felix"
                                                   :timestamp "1337"}
                                           :content ["foobar"]}]}))))

(deftest test-unmarshal-message-with-channels
  (is (= {:channels "Users,Important" :body ""}
         (unmarshal-message {:attrs {:channels "Users, Important"}
                             :content [""]}))))
