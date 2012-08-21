(defproject de.viaboxx.flurfunk/flurfunk-server "1.1-SNAPSHOT" 
  :description "The Funk of the Flur"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/java.jdbc "0.2.3"]
                 [compojure/compojure "0.6.5"]
                 [fleetdb-client "0.2.2"]
                 [postgresql/postgresql "9.1-901.jdbc4"]
                 [ring/ring-jetty-adapter "0.3.11"]]
  :plugins [[lein-ring "0.6.5"]]
  :ring {:handler flurfunk.server.routes/app}
  :main flurfunk.server.jetty
  :uberjar-name "flurfunk-server.jar")
