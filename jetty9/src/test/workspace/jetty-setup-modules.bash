#!/bin/bash
set - java -jar start.jar arg
JETTY_BASE="/jetty/base"
JETTY_PROPERTIES="prop0=value0,prop1=value1"
JETTY_MODULES_ENABLE="mod0,mod1"
JETTY_MODILES_DISABLE="gone0,gone1"

touch $JETTY_BASE/start.d/gone0.ini
touch $JETTY_BASE/start.d/gone1.ini

source /setup-env.d/50-jetty.bash
if [ "$(echo ${JETTY_ARGS} | xargs)" != "prop0=value0 prop1=value1 --module=mod0 --module=mod1" ]; then 
  echo "JETTY_ARGS='$(echo ${JETTY_ARGS} | xargs)'"
elif [ "$(echo $@ | xargs)" != "java -Djetty.base=/jetty/base -jar start.jar arg prop0=value0 prop1=value1 --module=mod0 --module=mod1" ]; then
  echo "@='$(echo $@ | xargs)'"
elif [ -x JETTY_BASE/start.d/gone0.ini ]; then
  echo gone0.ini not deleted
elif [ -x JETTY_BASE/start.d/gone1.ini ]; then
  echo gone1.ini not deleted
else
  echo OK
fi
