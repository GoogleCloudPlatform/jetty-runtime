#!/bin/bash
set - zero arg
JAVA_OPTS="-X"
JETTY_PROPERTIES="prop0=value0,prop1=value1"
JETTY_MODULES_ENABLE="mod0,mod1"
JETTY_MODILES_DISABLE="gone0,gone1"

touch $JETTY_BASE/start.d/gone0.ini
touch $JETTY_BASE/start.d/gone1.ini

source /setup-env.d/50-jetty.bash
if [ "$(echo ${JETTY_ARGS} | xargs)" != "arg prop0=value0 prop1=value1 --module=mod0 --module=mod1" ]; then 
  echo "JETTY_ARGS='${JETTY_ARGS}'"
elif [ "$(echo ${JAVA_OPTS} | xargs)" != "-X arg prop0=value0 prop1=value1 --module=mod0 --module=mod1" ]; then
  echo "JAVA_OPTS='${JAVA_OPTS}'"
elif [ -x JETTY_BASE/start.d/gone0.ini ]; then
  echo gone0.ini not deleted
elif [ -x JETTY_BASE/start.d/gone1.ini ]; then
  echo gone1.ini not deleted
else
  echo OK
fi
