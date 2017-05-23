#!/bin/bash

EXPECTED_DBG_AGENT="-agentpath:/opt/cdbg/cdbg_java_agent.so=--log_dir=/var/log/app_engine,--alsologtostderr=true,--cdbg_extra_class_path=${JETTY_BASE}/webapps/root/WEB-INF/classes:${JETTY_BASE}/webapps/root/WEB-INF/lib"
ACTUAL_DBG_AGENT="$(export GAE_INSTANCE=instance; /docker-entrypoint.bash env | grep DBG_AGENT | cut -d '=' -f 1 --complement)"

if [ $ACTUAL_DBG_AGENT != "$EXPECTED_DBG_AGENT" ]; then
  echo "DBG_AGENT='$(echo ${ACTUAL_DBG_AGENT})'"
  exit 1
else
  echo OK
fi
