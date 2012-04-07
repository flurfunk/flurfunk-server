(defproject de.viaboxx.flurfunk/flurfunk-server "0.1.0-SNAPSHOT" 
  :description "The Funk of the Flur"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [compojure/compojure "0.6.5"]
                 [fleetdb-client "0.2.2"]
                 [ring/ring-jetty-adapter "0.3.11"]]
  :plugins [[lein-ring "0.6.3"]]
  :ring {:handler flurfunk.server.routes/flurfunk-server}
  :main flurfunk.server.jetty)
