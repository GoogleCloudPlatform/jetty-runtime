#!/bin/bash

DIR=$(dirname $0)

if [ ! -f $JETTY_START_COMMAND ]; then
  export JETTY_START_COMMAND=$(source $DIR/gen_start_command.sh)
fi

echo $JETTY_START_COMMAND

set -- $(cat $JETTY_START_COMMAND)
source /docker-entrypoint.bash

