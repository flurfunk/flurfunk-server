(ns flurfunk.core
  "The core service of Flurfunk, provides the index page and the REST API."
  (:use compojure.core
        ring.util.servlet
        hiccup.core)
  (:require [compojure.route :as route])
  (:gen-class
   :extends javax.servlet.http.HttpServlet))

;; TODO: Put this into a separate storage namespace

(def messages [])

(defn get-messages []
  messages)

(defn find-message [id]
  (first (filter (fn [message] (= id (:id message)))
                 (get-messages))))

(defn marshal-message [message]
  (str "<message id='" (:id message)
       "' author='" (:author message)
       "'>" (:body message) "</message>"))

(defn marshal-messages [messages]
  (str "<messages>"
       (let [messages (get-messages)]
         (if (not (empty? messages))
           (reduce (fn [x y] (str x y))
                   (map (fn [message]
                          (marshal-message message))
                        messages))))
       "</messages>"))

(defroutes main-routes
  (GET "/" [] (html
               [:head [:title "Flurfunk"]]
               [:body
                [:script {:src "flurfunk.js"}]]))
  (GET "/messages" []
       (marshal-messages (get-messages)))

  (GET "/message/:id" [id]
       (if-let [message (find-message id)]
         (marshal-message (find-message id))
         {:body "" :status 404}))
  (route/files "/" {:root "src/main/webapp"})
  (route/not-found "Page not found"))

(defservice main-routes)
