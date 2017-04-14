#!/bin/bash
set - java start.jar two three
JAVA_OPTS="-java -options"
source /setup-env.d/50-jetty.bash
if [ "$(echo ${JETTY_ARGS} | xargs)" != "start.jar two three" ]; then 
  echo "JETTY_ARGS='${JETTY_ARGS}'"
elif [ "$(echo ${JAVA_OPTS} | xargs)" != "-java -options start.jar two three" ]; then
  echo "JAVA_OPTS='${JAVA_OPTS}'"
else
  echo OK
fi
