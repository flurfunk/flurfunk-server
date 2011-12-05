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
    
>    (require '[flurfunk-server.routes :as routes] :reload)

Then you can call functions like this:

    (routes/app)

Running the tests
-----------------

    lein test

Running the server
------------------

    lein ring server-headless

Testing together with the web frontend
--------------------------------------

    cd ..
    git clone git@viaboxxsystems.de:flurfunk/flurfunk-web.git
    cd flurfunk-web
    lein cljs war
    unzip flurfunk-web.war -d ../flurfunk-server/resources/public
    cd ../flurfunk-server
    lein ring server

Creating a WAR
--------------

Remember to remove the resources copied from the web frontend before building a
WAR.

    mkdir temp
    lein ring uberwar temp/flurfunk-server.war

Deploying WAR to Nexus
--------------

After completing the above step of creating a WAR, you can deploy it to Nexus
like this:

    lein pom temp/pom.xml

    mvn deploy:deploy-file -Durl=https://server/nexus/content/repositories/snapshots/ \
                       -DrepositoryId=viaboxx-snapshots \
                       -Dfile=temp/flurfunk-server.war \
                       -DpomFile=temp/pom.xml \
                       -Dpackaging=war 

TODO: Document how to do a release (hint, like the above, but with releases
instead of snapshots, and a real version).

Downloading WAR from Nexus
--------------

After deploying to Nexus, you might want to download the WAR to a server where
it can be deployed into a container:

    wget -O flurfunk-server.war --user=jenkins-artifacts --password=PASSWORD \
        'https://server/nexus/service/local/artifact/maven/redirect?r=snapshots&g=de.viaboxx.flurfunk&a=flurfunk-server&v=0.1.0-SNAPSHOT&e=war'

Using a persistent database
---------------------------

Messages are per default stored in memory. For a persistent database, install
fleetdb:

    mkdir temp
    cd temp
    curl -O http://fleetdb.s3.amazonaws.com/fleetdb-standalone.jar

Then launch the fleetdb server:

    scripts/fleetdb-flurfunk

where flurfunk.fdb is a pure text file where stuff will be stored.

Finally, run the server like this:

    JAVA_OPTS="-Dflurfunk.fleetdb=true" lein ring server-headless
