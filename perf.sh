if [ -z "$1" ]
then
   TIME=10
else
   TIME=$1
fi

mvn  install  -pl :perf-server,:perf-runner,:perf-test -Pgcloud-deploy -Drunning.time=$TIME  -Pperf-test -am -Pperf
