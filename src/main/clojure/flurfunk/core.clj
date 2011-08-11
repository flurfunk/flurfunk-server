(ns flurfunk.core
  "The core service of Flurfunk, provides the index page and the REST API."
  (:use compojure.core
        ring.util.servlet
        hiccup.core)
  (:require [compojure.route :as route]
            [clojure.xml :as xml]
            [clojure.contrib.duck-streams :as streams]
            [clojure.contrib.io :as io]
            [flurfunk.marshalling :as ms])
  (:gen-class
   :extends javax.servlet.http.HttpServlet))

;; TODO: Put this into a separate storage namespace

(def messages [])

(defn get-messages []
  messages)

(defn add-message [message])

(defn find-message [id]
  (first (filter (fn [message] (= id (:id message)))
                 (get-messages))))

(defn parse-message [s]
  (let [xml (xml/parse (io/input-stream (streams/to-byte-array s)))]
       (ms/unmarshal-message xml)))

(defroutes main-routes
  (GET "/" [] (html
               [:head [:title "Flurfunk"]]
               [:body
                [:script {:src "flurfunk.js"}]]))
  (GET "/messages" []
       (ms/marshal-messages (get-messages)))
  (GET "/message/:id" [id]
       (if-let [message (find-message id)]
         (ms/marshal-message (find-message id))
         {:body "" :status 404}))
  (POST "/message" {body :body} (add-message (parse-message body))
        "")
  (route/files "/" {:root "src/main/webapp"})
  (route/not-found "Page not found"))

(defservice main-routes)
