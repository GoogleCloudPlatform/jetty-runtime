#!/bin/bash
set - java start.jar two three
JAVA_OPTS="-java -options"
source /setup-env.d/50-jetty.bash
if [ "$(echo ${JETTY_ARGS} | xargs)" != "" ]; then 
  echo "JETTY_ARGS='$(echo ${JETTY_ARGS} | xargs)'"
elif [ "$(echo $@ | xargs)" != "java start.jar two three" ]; then
  echo "@='$(echo $@ | xargs)'"
else
  echo OK
fi
