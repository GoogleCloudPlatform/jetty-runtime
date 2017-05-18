#!/bin/bash

rm -fr $JETTY_BASE/webapps/root $JETTY_BASE/webapps/root.war
mkdir /app
trap "rm -rf /app" EXIT
echo original > /app/index.html

source /setup-env.d/50-jetty.bash

if [ "$(cat $JETTY_BASE/webapps/root/index.html)" != "original" ]; then
  echo FAILED not linked to /app
  exit 1
fi

echo OK

