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

    lein ring server-headless [port]

### Creating a WAR ###

    lein ring uberwar flurfunk-server.war

This will create _target/flurfunk-server.war_.

### Creating a standalone JAR that includes Jetty ###

    lein uberjar

### Running the standalone JAR ###

    java -jar target/flurfunk-server.jar

### Using a persistent database ###

Messages are per default stored in memory. There are two options for a
persistent storage:

#### PostgreSQL ####

Flurfunk requires PostgreSQL 9.1 or above, and the _hstore_ addon.

Create a user _flurfunk_ with password _flurfunk_ and a database
_flurfunk_, or include the system property `flurfunk.db.url` to a JDBC
URL string like this:

    -Dflurfunk.db.url=-Dflurfunk.db.url=postgresql://flurfunk:xxxxxxxxx@localhost:5432/flurfunk

Then run the server with the _flurfunk.db_ system property set to
_postgresql_, like this (optionally including the extra url parameter
from above):

    JAVA_OPTS="-Dflurfunk.db=postgresql" lein ring server-headless

#### FleetDB ####

    mkdir temp
    cd temp
    curl -O http://fleetdb.s3.amazonaws.com/fleetdb-standalone.jar

Then launch the fleetdb server:

    scripts/fleetdb-flurfunk

Finally, run the server with the _flurfunk.db_ system property
set to _fleetdb_, like this:

    JAVA_OPTS="-Dflurfunk.db=fleetdb" lein ring server-headless

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
