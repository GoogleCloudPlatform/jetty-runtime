#!/bin/bash

rm -fr $JETTY_BASE/webapps/root $JETTY_BASE/webapps/root.war
mkdir $JETTY_BASE/webapps/root
echo original > $JETTY_BASE/webapps/root/index.html
cd $JETTY_BASE/webapps/root
jar cf ../root.war *
cd ..

rm -fr $JETTY_BASE/webapps/root
source /setup-env.d/50-jetty.bash

if [ "$(cat $JETTY_BASE/webapps/root/index.html)" != "original" ]; then
  echo FAILED not unpacked when no root
  exit 1
fi

echo updated > $JETTY_BASE/webapps/root/index.html
source /setup-env.d/50-jetty.bash
if [ "$(cat $JETTY_BASE/webapps/root/index.html)" != "updated" ]; then
  echo FAILED unpacked when war older
  exit 1
fi

sleep 1

touch $JETTY_BASE/webapps/root.war
source /setup-env.d/50-jetty.bash
if [ "$(cat $JETTY_BASE/webapps/root/index.html)" != "original" ]; then
  echo FAILED not unpacked when war newer
  exit 1
fi


echo OK

