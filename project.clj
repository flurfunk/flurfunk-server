(defproject flurfunk "0.1.0-SNAPSHOT"
  :description "The Funk of the Flur"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [compojure/compojure "0.6.5"]
                 [fleetdb-client "0.2.2"]]
  :dev-dependencies [[lein-ring "0.4.5"]]
  :ring {:handler flurfunk.routes/app})