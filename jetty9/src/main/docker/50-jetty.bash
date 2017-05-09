#!/bin/bash
# Configure the environment and CMD for the Jetty Container

# Unpack a WAR app (if present) beforehand so that Stackdriver Debugger
# can load it. This should be done before the JVM for Jetty starts up.
export ROOT_WAR=$JETTY_BASE/webapps/root.war
export ROOT_DIR=$JETTY_BASE/webapps/root
if [ -e "$ROOT_WAR" ]; then
  # Unpack it only if $ROOT_DIR doesn't exist or the root is older than the war.
  if [ -e "$ROOT_WAR" -a \( \( ! -e "$ROOT_DIR" \) -o \( "$ROOT_DIR" -ot "$ROOT_WAR" \) \) ]; then
    rm -fr $ROOT_DIR
    unzip $ROOT_WAR -d $ROOT_DIR
    chown -R jetty:jetty $ROOT_DIR
  fi
fi

# If webapp root not available, then try /app as used by gcloud maven plugin
if [ ! -e "$ROOT_DIR" -a -d /app ]; then
  ln -s /app "$ROOT_DIR"
fi

# move command line args to $JETTY_ARGS
export JETTY_ARGS="${@/$1/}"
set - "$1"

# Check for start.jar
if [ "$(echo $JETTY_ARGS | egrep start.jar | wc -l )" = "0" ]; then
  JETTY_ARGS="-Djetty.base=${JETTY_BASE} -jar ${JETTY_HOME}/start.jar $JETTY_ARGS"
fi

# Add any Jetty properties to the JETTY_ARGS
if [ "$JETTY_PROPERTIES" ]; then
  JETTY_ARGS="$JETTY_ARGS ${JETTY_PROPERTIES//,/ }"
fi

# Enable jetty modules
if [ "$JETTY_MODULES_ENABLE" ]; then
  for M in ${JETTY_MODULES_ENABLE//,/ }; do
    JETTY_ARGS="$JETTY_ARGS --module=$M"
  done
fi

# Disable jetty modules
if [ "$JETTY_MODULES_DISABLE" ]; then
  for M in ${JETTY_MODULES_DISABLE//,/ }; do
    rm -f ${JETTY_BASE}/start.d/${M}.ini
  done
fi

# If we are deployed on a GAE platform, enable the gcp module
if [ "$PLATFORM" = "gae" ]; then
  JETTY_ARGS="$JETTY_ARGS --module=gcp"
fi

# Add the JETTY_ARGS to the JAVA_OPTS
export JAVA_OPTS="$JAVA_OPTS $JETTY_ARGS"

# Set CDBG_APP_WEB_INF_DIR, used by CDBG in format-env-appengine-vm.sh
if [ "$DBG_ENABLE" = "true" ]; then
  CDBG_APP_WEB_INF_DIR="${JETTY_BASE}/webapps/root/WEB-INF"
fi
