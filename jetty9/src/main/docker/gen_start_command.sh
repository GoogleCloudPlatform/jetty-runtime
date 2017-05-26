#!/bin/sh

START_COMMAND_OUT=$1

java -jar -Djetty.base=${JETTY_BASE} ${JETTY_HOME}/start.jar $JETTY_ARGS --dry-run --exec-properties="${START_COMMAND_OUT}.properties" &> $START_COMMAND_OUT
