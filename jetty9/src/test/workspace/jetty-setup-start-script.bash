#!/bin/bash

CUSTOM_START_CMD="echo 'pre-generated start command'"
export JETTY_START_COMMAND=$CUSTOM_START_CMD
source /scripts/jetty/jetty_start.sh
if [ $JETTY_START_COMMAND != $CUSTOM_START_CMD ]; then
  echo 'Expected $JETTY_START_COMMAND not to be overwritten'
  exit 1
fi
