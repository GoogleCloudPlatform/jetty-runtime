#!/bin/bash

# ensure non-jetty commands are left alone
set - java -version
source /setup-env.d/60-jetty-dry-run.bash
if [ "$(echo $@ | xargs)" != "java -version" ]; then
  echo "UNEXPECTED: @='$(echo $@ | xargs)'"
  exit 1
fi

# ensure the contents of /jetty-start are honored
echo 'this is a pre-generated jetty-start command' > /jetty-start
set - java -jar $JETTY_HOME/start.jar
source /setup-env.d/60-jetty-dry-run.bash
if [ "$(echo $@ | xargs)" != "this is a pre-generated jetty-start command" ]; then
  echo "UNEXPECTED: @='$(echo $@ | xargs)'"
  exit 1
fi
rm -f /jetty-start

# ensure the generation of dry-run commands works
set - java -jar $JETTY_HOME/start.jar
source /setup-env.d/60-jetty-dry-run.bash
if [ "$(echo $@ | egrep start.jar | wc -l )" != "0" ]; then
  # fail if the expression contains an invocation of start.jar
  echo "UNEXPECTED: @='$(echo $@ | xargs)'"
  exit 1
fi

echo 'OK'
