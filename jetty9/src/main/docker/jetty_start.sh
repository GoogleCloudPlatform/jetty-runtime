#!/bin/bash

JETTY_START_COMMAND="/scripts/jetty/jetty-start-command"

if [ ! -f $JETTY_START_COMMAND ]; then
  java -jar -Djetty.base=${JETTY_BASE} ${JETTY_HOME}/start.jar $JETTY_ARGS --dry-run --exec-properties=exec.properties &> $JETTY_START_COMMAND

  chmod +x $JETTY_START_COMMAND

  echo "Generated jetty start command:"
  cat $JETTY_START_COMMAND
fi

set -- $(cat $JETTY_START_COMMAND)
source /docker-entrypoint.bash

