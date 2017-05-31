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

# If a webapp root directory exist, use it as the work directory
if [ -d "$ROOT_DIR" ]; then
  cd "$ROOT_DIR"
fi

# Calculate the Jetty args
export JETTY_ARGS

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

# If this is a jetty command
if expr "$*" : '^java .*/start\.jar.*$' >/dev/null ; then
  # Append JETTY_ARGS if available
  if [ -n "$JETTY_ARGS" ]; then
    shift
    set -- java $@ $JETTY_ARGS
  fi

  # check if it is a terminating command
  for A in "$@" ; do
    case $A in
      --add-to-start* |\
      --create-files |\
      --create-startd |\
      --download |\
      --dry-run |\
      --exec-print |\
      --help |\
      --info |\
      --list-all-modules |\
      --list-classpath |\
      --list-config |\
      --list-modules* |\
      --stop |\
      --update-ini |\
      --version |\
      -v )\
      # It is a terminating command, so exec directly
      exec "$@"
    esac
  done

  # Check if a start command has already been generated
  if [ -f /jetty-start ] ; then
    if [ $JETTY_BASE/start.d -nt /jetty-start ] ; then
      cat >&2 <<- 'EOWARN'
      ********************************************************************
      WARNING: The $JETTY_BASE/start.d directory has been modified since
               the /jetty-start files was generated. Please either delete
               the /jetty-start file or re-run
               /scripts/jetty/generate-jetty-start.sh from a Dockerfile
      ********************************************************************
EOWARN
    fi

    echo $(date +'%Y-%m-%d %H:%M:%S.000'):INFO:docker-entrypoint:jetty start command from /jetty-start
    set -- $(cat /jetty-start)
  else
    # Do a jetty dry run to set the final command
    set -- $("$@" --dry-run --exec-properties=$(mktemp --suffix=.properties))
  fi
fi
