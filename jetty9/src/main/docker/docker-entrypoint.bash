#!/bin/bash

# source the supported feature JVM arguments
source /setup-env.bash
  
# default jetty arguments
export DEFAULT_ARGS="-Djetty.base=$JETTY_BASE -jar $JETTY_HOME/start.jar"

# Unpack a WAR app (if present) beforehand so that Stackdriver Debugger
# can load it. This should be done before the JVM for Jetty starts up.
export ROOT_WAR=$JETTY_BASE/webapps/root.war
export ROOT_DIR=$JETTY_BASE/webapps/root
if [ -e "$ROOT_WAR" ]; then
  # Unpack it only if $ROOT_DIR doesn't exist or the root is older than the war.
  if [ -e "$ROOT_WAR" -a \( \( ! -e "$ROOT_DIR" \) -o \( "$ROOT_DIR" -ot "$ROOT_WAR" \) \) ]; then
    unzip $ROOT_WAR -d $ROOT_DIR
  fi
fi

# If webapp root not available, then try /app as used by gcloud maven plugin
if [ ! -e "$ROOT_DIR" -a -d /app ]; then
  ln -s /app "$ROOT_DIR"
fi

# If the passed arguments start with the java command
if [ "java" = "$1" -o "$(which java)" = "$1" ] ; then
  # ignore the java command as it is the default
  shift
  # clear the default args, use the passed args
  DEFAULT_ARGS=
fi

# If the first argument is not executable
if ! type "$1" &>/dev/null; then
  # then treat all arguments as arguments to the java command
  
  # set the command line to java with the feature arguments and passed arguments
  set -- java $JAVA_OPTS $DEFAULT_ARGS "$@"
fi

exec "$@"
