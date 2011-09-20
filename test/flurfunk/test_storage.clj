(ns flurfunk.test-storage
  (:use clojure.test
        flurfunk.storage))

(deftest test-add-and-get-message
  (add-message {:author "felix" :body "foobar"})
  (let [first-message (first (get-messages))]
    (are [v k] (= v (k first-message))
	 "foobar" :body
	 "felix" :author)
    (is (not (empty? (:id first-message)))
	"An ID should be generated for the new message.")))

(deftest test-find-message
  (add-message {:author "felix" :body "foobar"})
  (let [id (:id (first (get-messages)))]
    (add-message {:author "thomas" :body "barfoo"})
    (let [message (find-message id)]
      (are [v k] (= v (k message))
	   "foobar" :body
	   "felix" :author))))

(deftest test-ordered-messages
  (add-message {:author "felix" :body "first"})
  (is (= "felix" (:author (first (get-messages)))))
  (add-message {:author "thomas" :body "second"})
  (is (= "thomas" (:author (first (get-messages))))))
