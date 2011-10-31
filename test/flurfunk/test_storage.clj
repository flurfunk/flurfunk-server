(ns flurfunk.test-storage
  (:use clojure.test
        flurfunk.storage))

(deftest test-add-and-get-message
  (clear-messages)
  (add-message {:author "felix" :timestamp 1337 :body "foobar"})
  (let [first-message (first (get-messages))]
    (are [v k] (= v (k first-message))
	 "foobar" :body
	 "felix" :author
         1337 :timestamp)
    (is (not (empty? (:id first-message)))
	"An ID should be generated for the new message.")))

(deftest test-find-message
  (clear-messages)
  (add-message {:author "felix" :timestamp 1337 :body "foobar"})
  (let [id (:id (first (get-messages)))]
    (add-message {:author "thomas" :timestamp 1338 :body "barfoo"})
    (let [message (find-message id)]
      (are [v k] (= v (k message))
	   "foobar" :body
	   "felix" :author
           1337 :timestamp))))

(deftest test-ordered-messages
  (clear-messages)
  (add-message {:author "felix" :timestamp 1337 :body "first"})
  (is (= "felix" (:author (first (get-messages)))))
  (add-message {:author "thomas" :timestamp 1338 :body "second"})
  (is (= "thomas" (:author (first (get-messages))))))

(deftest test-messages-since
  (clear-messages)
  (add-message {:author "felix" :timestamp 10337 :body "first"})
  (add-message {:author "thomas" :timestamp 10338 :body "second"})
  (is (= 1 (count (get-messages 10337)))))


(deftest limit-messages
  (dotimes [n 300] (add-message {:author "felix" :timestamp 1337 :body (str "n is " n)}))
  (is (= 200 (count (get-messages)))))
