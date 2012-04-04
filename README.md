Flurfunk server
===============

The Flurfunk server is written in Clojure, and built using Leiningen.
It provides a REST API for use by clients.

Building and running
--------------------

### Installing Leiningen 2 ###

    curl -O https://raw.github.com/technomancy/leiningen/preview/bin/lein
    chmod +x lein
    mv lein ~/bin/ # Make sure that ~/bin/ exists and is on the $PATH

### Running the tests ###

    lein test

### Running the server ###

    lein ring server-headless

### Testing together with the web frontend ###

    lein ring server 4000
    cd ../flurfunk-web # checked out from git
    lein cljs compile-dev
    lein ring server-headless

Then go to http://localhost:3000/index-dev.html and enter
http://localhost:4000 as the server URL.

### Creating a WAR ###

    mkdir temp
    lein ring uberwar temp/flurfunk-server.war

### Creating a standalone JAR that includes Jetty ###

    lein uberjar
    mv target/flurfunk-server-*-standalone.jar temp/flurfunk-server.jar

### Running the standalone JAR ###

    java -jar temp/flurfunk-server.jar -Dflurfunk.port=8080

### Deploying WAR to Nexus ###

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

### Downloading WAR from Nexus ###

After deploying to Nexus, you might want to download the WAR to a server where
it can be deployed into a container:

    wget -O flurfunk-server.war --user=jenkins-artifacts --password=PASSWORD \
        'https://server/nexus/service/local/artifact/maven/redirect?r=snapshots&g=de.viaboxx.flurfunk&a=flurfunk-server&v=0.1.0-SNAPSHOT&e=war'

### Using a persistent database ###

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

License
-------

Copyright 2012 Viaboxx GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
