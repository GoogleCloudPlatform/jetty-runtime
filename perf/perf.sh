#!/usr/bin/env bash

# required options
RUNNING_TIME="required"
APP_NAME="required"
APP_PROJECT="required"

# defaults
SKIP_CLIENT_DEPLOY="false"
SKIP_SERVER_DEPLOY="false"
RUNNER_INSTANCES=1
TRANSACTION_RATE=2800
VERBOSE="false"
LOG_FILE="perf.log"

# test config defaults
TEST_LATENCY_RANGE=4000-6000
TEST_QPS_RANGE=4900-5600

while [[ $# -ge 1 ]]
do
key="$1"

case $key in
    -rt|--running-time)
    RUNNING_TIME="$2"
    shift
    shift
    ;;
    -tr|--transaction-rate)
    TRANSACTION_RATE="$2"
    shift
    shift
    ;;
    -ri|--runner-instances)
    RUNNER_INSTANCES="$2"
    shift
    shift
    ;;
    -tlr|--test-latency-range)
    TEST_LATENCY_RANGE="$2"
    shift
    shift
    ;;
    -tqr|--test-qps-range)
    TEST_QPS_RANGE="$2"
    shift
    shift
    ;;
    --app-name)
    APP_NAME="$2"
    shift
    shift
    ;;
    --app-project)
    APP_PROJECT="$2"
    shift
    shift
    ;;
    -scd|--skip-client-deploy)
    SKIP_CLIENT_DEPLOY="true"
    shift
    ;;
    -ssd|--skip-server-deploy)
    SKIP_SERVER_DEPLOY="true"
    shift
    ;;
    --verbose)
    VERBOSE="true"
    shift
    ;;
    --log-file)
    LOG_FILE="$2"
    shift
    shift
    ;;
    *)
    echo "unknown option: $1"
    shift
    ;;
esac
done

if [ "$RUNNING_TIME" == "required" ] ||
   [ "$APP_NAME" == "required" ] ||
   [ "$APP_PROJECT" == "required" ]; then
  echo "Usage: perf.sh --app-name <name> --app-project <project> --running-time # (in minutes)"
  echo "  required settings"
  echo "   [ --app-name <name> ]"
  echo "   [ --app-project <project> ]"
  echo "   [ -rt # | --running-time # ]"
  echo "  client settings"
  echo "   [ -tr # | --transaction-rate # ]"
  echo "   [ -ri # | --runner-instances # ]"
  echo "  test settings"
  echo "   [ -tlr #-# | --test-latency-range #-# ]"
  echo "   [ -tqr #-# | --test-qps-range #-# ]"
  echo "  runtime settings"
  echo "   [ -scd | --skip-client-deploy ]"
  echo "   [ -ssd | --skip-server-deploy ]"
  echo "   [ --verbose ]"
  echo "   [ --log-file <filename> ] (perf.log)"
  exit
fi

echo "Perf Test "

if [ "$VERBOSE" != "true" ]; then
  echo " progress: tail -f $LOG_FILE";
fi

echo " Server Settings:"

if [ "$SKIP_SERVER_DEPLOY" == "true" ]; then
  echo "(skipping server deploy)"
else
  echo "(installing server, this may take a few minutes)"
  if [ "$VERBOSE" == "true" ]; then
    mvn install -pl :perf-server -Pgcloud-deploy;
  else
    mvn install -pl :perf-server -Pgcloud-deploy > $LOG_FILE 2>&1;
  fi
fi

echo " Client Settings:"
echo " - runner instances: $RUNNER_INSTANCES"
echo " - running time: $RUNNING_TIME"
echo " - transaction rate: $TRANSACTION_RATE"

if [ "$SKIP_CLIENT_DEPLOY" == "true" ]; then
   echo "(skipping client deploy)"
#   echo " APP_NAME: $APP_NAME"
#   echo " APP_PROJECT: $APP_PROJECT"
   CLIENT_URL="https://$APP_NAME-dot-$APP_PROJECT.appspot.com"
   echo "(checking for live runner config)"
   CLIENT_STATUS=`curl $CLIENT_URL/status 2>/dev/null`
   if [[ $CLIENT_STATUS != \{* ]] || [[ $CLIENT_STATUS == *RUNNING* ]]; then
     echo "INVALID RUNNER: $CLIENT_STATUS"
     echo "(aborting)"
     exit
   fi

   CLIENT_RUN_JSON="{\"profileXmlPath\":null,\"profileJsonPath\":null,
    \"profileGroovyPath\":\"/loadgenerator_profile.groovy\",
    \"host\":\"jetty-runtime-perf-app-dot-jetty9-work.appspot.com\",\"port\":443,\"users\":12,
    \"transactionRate\":$TRANSACTION_RATE,\"transport\":\"HTTP\",\"selectors\":1,\"threads\":1,\"runningTime\":$RUNNING_TIME,
    \"runningTimeUnit\":\"MINUTES\",\"runIteration\":0,\"reportHost\":\"localhost\",\"scheme\":\"https\",
    \"reportPort\":0,\"notInterrupt\":false,\"statsFile\":null,\"params\":{\"jettyRun\":\"true\",\"noSysExit\":\"true\",
    \"jettyPort\":\"8080\"},\"help\":false,\"displayStatsAtEnd\":false,\"collectServerStats\":false,\"warmupNumber\":2,
    \"maxRequestsQueued\":410000,\"channelPerUser\":-1}"

   echo "(init test run)"
   curl -H "Content-Type: application/json" -X POST -d "$CLIENT_RUN_JSON" $CLIENT_URL/run

else
  echo "(installing client, this may take a few minutes)";
  if [ "$VERBOSE" == "true" ]; then
    mvn install  -pl :perf-runner -Pgcloud-deploy -Pperf \
      -Drunner.instances=$RUNNER_INSTANCES \
      -Drunning.time=$RUNNING_TIME \
      -Dtransaction.rate=$TRANSACTION_RATE;
  else
    mvn install  -pl :perf-runner -Pgcloud-deploy -Pperf \
      -Drunner.instances=$RUNNER_INSTANCES \
      -Drunning.time=$RUNNING_TIME \
      -Dtransaction.rate=$TRANSACTION_RATE \
      >> $LOG_FILE 2>&1;
  fi
fi

echo " Test Settings:"
echo " - latency range: $TEST_LATENCY_RANGE"
echo " - qps range: $TEST_QPS_RANGE"
echo "(running perf test)";

mvn install  -pl :perf-test \
  -Dtest.latency.range=$TEST_LATENCY_RANGE \
  -Dtest.qps.range=$TEST_QPS_RANGE

