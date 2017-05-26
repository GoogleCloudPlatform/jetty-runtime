#!/bin/sh

java -jar -Djetty.base=${JETTY_BASE} ${JETTY_HOME}/start.jar $JETTY_ARGS --dry-run
