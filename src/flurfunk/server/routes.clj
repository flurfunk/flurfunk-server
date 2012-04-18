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

(defn- parse-timestamp [s]
  (if (or (not s) (= s "NaN"))
    nil
   (Long. s)))

(defroutes main-routes
  (GET "/" {uri :uri}
       (response/redirect (str uri (if (not (.endsWith uri "/")) "/")
                               "index.html")))
  (GET "/messages" {params :params}
       (ms/marshal-messages
        (let [since (parse-timestamp (:since params))
              before (parse-timestamp (:before params))
              opts {:since since :before before}
              opts (apply dissoc opts
                          (for [[k v] opts :when (nil? v)] k))]
          (if (empty? opts)
            (storage/get-messages)
            (storage/get-messages opts)))))
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

(def app
     (handler/site main-routes))
