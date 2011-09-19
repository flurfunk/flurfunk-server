(ns flurfunk.routes
  "The routes of Flurfunk."
  (:use compojure.core
        ring.util.servlet
        hiccup.core
        [hiccup.middleware :only (wrap-base-url)])
  (:require [clojure.xml :as xml]
            [clojure.contrib.duck-streams :as streams]
            [clojure.contrib.io :as io]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [flurfunk.marshalling :as ms]
            [flurfunk.storage :as storage])
  (:gen-class
   :extends javax.servlet.http.HttpServlet))

(defn parse-message [s]
  (let [xml (xml/parse (io/input-stream (streams/to-byte-array s)))]
    (ms/unmarshal-message xml)))

(defroutes main-routes
  (GET "/" [] (html
               [:head
                [:meta {:charset "utf-8"}]
                [:title "Flurfunk"]
                [:link {:rel "stylesheet" :type "text/css"
                        :href "flurfunk.css"}]]
               [:body
                [:script {:src "flurfunk.js"}]]))
  (GET "/messages" []
       (ms/marshal-messages (storage/get-messages)))
  (GET "/message/:id" [id]
       (if-let [message (storage/find-message id)]
         (ms/marshal-message message)
         {:body "" :status 404}))
  (POST "/message" {body :body} (storage/add-message (parse-message body))
        "")
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
     (-> (handler/site main-routes)
         (wrap-base-url)))
