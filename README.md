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

    lein ring server-headless

Testing together with the web frontend
--------------------------------------

    cd ..
    git clone git@viaboxxsystems.de:flurfunk/flurfunk-web.git
    cd flurfunk-web
    make war
    unzip flurfunk-web.war -d ../flurfunk-server/resources/public
    cd ../flurfunk-server
    lein ring server

Creating a WAR
--------------

Remember to remove the resources copied from the web frontend before building a WAR.

    mkdir temp
    lein ring uberwar temp/flurfunk-server.war

Deploying WAR to Nexus
--------------

After completing the above step of creating a WAR, you can deploy it to Nexus like this:

    lein pom temp/pom.xml

    mvn deploy:deploy-file -Durl=https://server/nexus/content/repositories/snapshots/ \
                       -DrepositoryId=viaboxx-snapshots \
                       -Dfile=temp/flurfunk-server.war \
                       -DpomFile=temp/pom.xml \
                       -DgroupId=de.viaboxx.flurfunk \
                       -DartifactId=flurfunk-server \
                       -Dversion=1.0-SNAPSHOT \
                       -Dpackaging=war 

TODO: Document how to do a release (hint, like the above, but with releases instead of snapshots, and a real version).

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

    JAVA_OPTS="-Dflurfunk.fleetdb=true" lein ring server-headless
