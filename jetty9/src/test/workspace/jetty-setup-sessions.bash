#!/bin/bash
set - java -jar start.jar two three
JETTY_BASE="/jetty/base"
JETTY_MODULES_ENABLE="gcp-datastore-sessions"
source /setup-env.d/50-jetty.bash
if [ "$(echo $@ | xargs)" != "java -Djetty.base=/jetty/base -jar start.jar two three --module=gcp-datastore-sessions" ]; then
  echo "@='$(echo $@ | xargs)'"
  exit 1
fi

echo OK
