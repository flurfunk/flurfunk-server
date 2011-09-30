Flurfunk server
===============

The Flurfunk server is written in Clojure, and built using Leiningen.
It provides a REST API for use by clients.

Installing Leiningen
--------------------

    curl -O https://raw.github.com/technomancy/leiningen/stable/bin/lein
    chmod +x lein
    mv lein ~/bin/ # Make sure that ~/bin/ exists and is on the $PATH

Downloading the dependencies
----------------------------

    lein deps

Running the REPL
----------------

    lein repl
    
In the REPL, import the namespace as follows (necessary after each code change):
    
    (require '[flurfunk.routes :as routes] :reload)

Then you can call functions like this:

    (routes/app)

Running the tests
-----------------

    lein test

Adding the web frontend
-----------------------

    cd ..
    git clone git@viaboxxsystems.de:flurfunk/flurfunk-web.git
    cd flurfunk-web
    make
    cp flurfunk.js flurfunk.css ../flurfunk-server/resources/public

Running the server
------------------

    lein ring server-headless 8080 

Creating a WAR
--------------

    mkdir temp
    lein ring uberwar temp/flurfunk-server.war

Using a persistent database
---------------------------

Messages are per default stored in memory. For a persistent database, install fleetdb:

    mkdir temp
    cd temp
    curl -O http://fleetdb.s3.amazonaws.com/fleetdb-standalone.jar

Then launch the fleetdb server:

    java -server -Xmx2g -cp fleetdb-standalone.jar fleetdb.server -f flurfunk.fdb

where flurfunk.fdb is a pure text file where stuff will be stored.

Finally, run the server like this:

    JAVA_OPTS="-Dflurfunk.fleetdb=true" lein ring server-headless 8080
