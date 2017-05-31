#!/bin/bash

rm -f /jetty-start /jetty-start.properties
/docker-entrypoint.sh --dry-run --exec-properties=/jetty-start.properties > /jetty-start
