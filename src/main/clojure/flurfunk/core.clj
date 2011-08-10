(ns flurfunk.core
  "The core service of Flurfunk, provides the index page and the REST API."
  (:use compojure.core
        ring.util.servlet
        hiccup.core)
  (:require [compojure.route :as route])
  (:gen-class
   :extends javax.servlet.http.HttpServlet))

(def messages [])

(defn get-messages []
  messages)

(defn marshal-messages [messages]
  (let [messages (get-messages)]
    (if (not (empty? messages))
      (reduce (fn [x y] (str x y))
              (map (fn [message]
                     (str "<message id='" (:id message)
                          "' author='" (:author message)
                          "'>" (:body message) "</message>"))
                   messages)))))

(defroutes main-routes
  (GET "/" [] (html
               [:head [:title "Flurfunk"]]
               [:body
                [:script {:src "flurfunk.js"}]]))
  (GET "/messages" []
       (str "<messages>"
            (marshal-messages (get-messages))
            "</messages>"))
  (route/files "/" {:root "src/main/webapp"})
  (route/not-found "Page not found"))

(defservice main-routes)
