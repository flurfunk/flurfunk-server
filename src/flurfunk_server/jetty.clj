(ns flurfunk-server.jetty
  "Runs Flurfunk in Jetty"
  (:use ring.adapter.jetty
        flurfunk-server.routes)
  (:gen-class))

(def default-port 8080)

(defn- get-port []
  (if-let [port (System/getProperty "flurfunk.port")]
    port
    (do (println (str "Using default port " default-port
                      ". Set the system property \"flurfunk.port\" "
                      "if you want a specific one."))
        default-port)))

(defn -main [& args]
  (let [port (get-port)]
    (try (run-jetty flurfunk-app {:port (Integer. port)})
         (catch NumberFormatException e
           (println (str "Invalid port number: '" port "'"))
           (System/exit 1)))))
