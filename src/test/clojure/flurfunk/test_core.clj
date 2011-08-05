(ns flurfunk.test-core
  (:use clojure.test
    clojure.contrib.string
    flurfunk.core))

(defn request [resource routes & params]
  (routes {:request-method :get :uri resource :params (first params)}))

(deftest test-index-page
  (is (substring? "flurfunk.js"
    (:body (request "/" main-routes)))))
