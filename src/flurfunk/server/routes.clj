(ns flurfunk.server.routes
  "The routes of Flurfunk."
  (:use compojure.core
        ring.util.servlet)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.util.response :as response]
            [flurfunk.server.marshalling :as ms]
            [flurfunk.server.storage :as storage]))

(defn- parse-message [s]
    (ms/unmarshal-message (ms/parse-xml s)))

(defroutes main-routes
  (GET "/" {uri :uri}
       (response/redirect (str uri (if (not (.endsWith uri "/")) "/")
                               "index.html")))
  (GET "/messages" {params :params}
       (ms/marshal-messages
        (let [since (:since params)]
          (if (and since (not (= since "NaN")))
            (storage/get-messages {:since (Long. since)})
            (storage/get-messages)))))
  (GET "/message/:id" [id]
       (if-let [message (storage/find-message id)]
         (ms/marshal-message message)
         {:body "" :status 404}))
  (POST "/message" {body :body}
        (storage/add-message (conj (parse-message body)
                                   {:timestamp (System/currentTimeMillis)}))
        "")
  (route/resources "/")
  (route/not-found "Page not found"))

(def flurfunk-server
     (handler/site main-routes))
