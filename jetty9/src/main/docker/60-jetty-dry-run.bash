#!/bin/bash

# Configure the start command for the Jetty Container, executing a dry-run if
# necessary.

# If this is a jetty command
if expr "$*" : '^java .*/start\.jar.*$' >/dev/null ; then

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
