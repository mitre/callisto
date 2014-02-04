#!/bin/sh

## you must run rmiregistry first (this is provided with the JDK), on
## the same port that you give the LPServer (default is 9999)
##
## e.g. rmiregistry 9999
##
## note: can't get the port stuff to work at the moment. just use the
## defaults for now (don't specify anything for rmiregistry).

CALLISTO_JAR=/afs/rcf/user/wmorgan/l/tallal/callisto/target/Callisto.jar
java -classpath $CALLISTO_JAR -Djava.rmi.server.codebase=file://$CALLISTO_JAR org.mitre.jawb.tasks.preannotate.LPServer
