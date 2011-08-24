(ns flurfunk.core
  "The core service of Flurfunk, provides the index page and the REST API."
  (:use compojure.core
        ring.util.servlet
        hiccup.core)
  (:require [compojure.route :as route]
            [clojure.xml :as xml]
            [clojure.contrib.duck-streams :as streams]
            [clojure.contrib.io :as io]
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
  (route/files "/" {:root "src/main/webapp"})
  (route/not-found "Page not found"))

(defservice main-routes)
