#!/bin/bash
set - zero one two three
JAVA_OPTS="-java -options"
source /setup-env.d/50-jetty.bash
if [ "$(echo ${JETTY_ARGS} | xargs)" != "one two three" ]; then 
  echo "JETTY_ARGS='${JETTY_ARGS}'"
elif [ "$(echo ${JAVA_OPTS} | xargs)" != "-java -options one two three" ]; then
  echo "JAVA_OPTS='${JAVA_OPTS}'"
else
  echo OK
fi
