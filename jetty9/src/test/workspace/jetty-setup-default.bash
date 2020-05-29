#!/bin/bash
set - java -jar start.jar two three
JETTY_BASE="/jetty/base"
source /setup-env.d/50-jetty.bash
if [ "$(echo ${JETTY_ARGS} | xargs)" != "" ]; then 
  echo "JETTY_ARGS='$(echo ${JETTY_ARGS} | xargs)'"
elif [ "$(echo $@ | xargs)" != "java -Djetty.base=/jetty/base -jar start.jar two three" ]; then
  echo "@='$(echo $@ | xargs)'"
else
  echo OK
fi
