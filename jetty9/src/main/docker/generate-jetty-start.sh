#!/bin/bash

rm -f /jetty-start /jetty-start.properties /jetty-start.args
GENERATE_JETTY_START=TRUE /docker-entrypoint.bash