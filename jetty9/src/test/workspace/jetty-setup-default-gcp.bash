#!/bin/bash
set - -zero one two three
JAVA_OPTS="-java -options"
PLATFORM=gae
source /setup-env.d/50-jetty.bash
if [ "$(echo ${JETTY_ARGS} | xargs)" != "-Djetty.base=/var/lib/jetty -jar /opt/jetty-home/start.jar -zero one two three --module=gcp" ]; then 
  echo "JETTY_ARGS='$(echo ${JETTY_ARGS} | xargs)'"
elif [ "$(echo ${JAVA_OPTS} | xargs)" != "-java -options -Djetty.base=/var/lib/jetty -jar /opt/jetty-home/start.jar -zero one two three --module=gcp" ]; then
  echo "JAVA_OPTS='$(echo ${JAVA_OPTS} | xargs)'"
else
  echo OK
fi
