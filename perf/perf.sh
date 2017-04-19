#!/usr/bin/env bash

# defaults
SKIP_CLIENT_DEPLOY="false"
SKIP_SERVER_DEPLOY="false"
RUNNER_INSTANCES=1
RUNNING_TIME="required"
TRANSACTION_RATE=2800
VERBOSE="false"
LOG_FILE="perf.log"

# test config defaults
TEST_LATENCY_RANGE=5000-6000
TEST_QPS_RANGE=4900-5200

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

if [ "$RUNNING_TIME" == "required" ]; then
  echo "Usage: perf.sh --running-time # (in minutes)"
  echo "  client settings"
  echo "   [ -rt # | --running-time # ]"
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
#echo " - skip server deploy: $SKIP_SERVER_DEPLOY"

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
   echo "TODO / support running perf test with already deployed runner instance"
else
  echo "(installing client, this may take a few minutes)";
  if [ "$VERBOSE" == "true" ]; then
    mvn install  -pl :perf-runner -Pgcloud-deploy -Pperf;
  else
    mvn install  -pl :perf-runner -Pgcloud-deploy -Pperf \
      -Drunner.instances=$RUNNER_INSTANCES \
      -Drunning.time=$RUNNING_TIME \
      -Dtransaction.rate=$TRANSACTION_RATE \
      > $LOG_FILE 2>&1;
  fi
fi

echo "(running perf test)";
mvn install  -pl :perf-test \
  -Dtest.latency.range=$TEST_LATENCY_RANGE \
  -Dtest.qps.range=$TEST_QPS_RANGE

