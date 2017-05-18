#!/bin/bash
# Enable quickstart module for Jetty and eagerly generate the quickstart-web.xml

source /docker-entrypoint.bash bash

# Generate configuration files for quickstart
if [ ! -e "$JETTY_BASE/webapps/root/WEB-INF/quickstart-web.xml" ]; then
  pushd ${JETTY_BASE}
  java -jar ${JETTY_HOME}/start.jar --add-to-start=quickstart
  QUICKSTART_CLASSPATH=$(java -jar ${JETTY_HOME}/start.jar --dry-run)
  # Replace the Xml configuration class with the Quickstart pre configuration class
  $( echo "$QUICKSTART_CLASSPATH" | sed 's/org.eclipse.jetty.xml.XmlConfiguration.*/org.eclipse.jetty.quickstart.PreconfigureQuickStartWar webapps\/root.war/')
  popd
fi
