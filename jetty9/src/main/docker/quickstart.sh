#!/bin/bash

# Enable quickstart module for Jetty and eagerly generate the quickstart-web.xml
if [ ! -e "$JETTY_BASE/webapps/root/WEB-INF/quickstart-web.xml" ]; then
  pushd ${JETTY_BASE}
  java -jar ${JETTY_HOME}/start.jar --add-to-start=quickstart
  $(dirname $0)/generate-jetty-start.sh

  JETTY_START_COMMAND=$(java -jar ${JETTY_HOME}/start.jar --dry-run)
   # Replace the Xml configuration class with the Quickstart pre configuration class
  QUICKSTART_START_COMMAND=$(echo ${JETTY_START_COMMAND} | sed 's/org.eclipse.jetty.xml.XmlConfiguration.*/org.eclipse.jetty.quickstart.PreconfigureQuickStartWar/')

  set -- "$JETTY_BASE/webapps/root"
  [ -e "$JETTY_BASE/webapps/root.war" ] && set -- "$JETTY_BASE/webapps/root.war" $@
  [ -e "$JETTY_BASE/webapps/root.xml" ] && set -- $@ "$JETTY_BASE/webapps/root.xml"
  /docker-entrypoint.bash ${QUICKSTART_START_COMMAND} $@
  popd
fi
