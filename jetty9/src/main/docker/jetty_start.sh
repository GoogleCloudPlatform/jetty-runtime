#!/bin/bash

DIR=$(dirname $0)
JETTY_START_COMMAND="/scripts/jetty/jetty-start-command"

if [ ! -f $JETTY_START_COMMAND ]; then
  source $DIR/gen_start_command.sh $JETTY_START_COMMAND
fi

set -- $(cat $JETTY_START_COMMAND)
source /docker-entrypoint.bash

