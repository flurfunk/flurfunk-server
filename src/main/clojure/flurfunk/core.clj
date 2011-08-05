(ns flurfunk.core
  "The core service of Flurfunk, provides the index page and the REST API."
  (:use compojure.core
    ring.util.servlet
    hiccup.core)
  (:require [compojure.route :as route])
  (:gen-class
    :extends javax.servlet.http.HttpServlet))

(defroutes main-routes
  (GET "/" [] (html
    [:head [:title "Flurfunk"]]
    [:body
     [:script {:src "flurfunk.js"}]]))
  (route/files "/" {:root "src/main/webapp"})
  (route/not-found "Page not found"))

(defservice main-routes)
