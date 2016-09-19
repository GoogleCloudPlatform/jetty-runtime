#!/bin/bash

function appengine_deploy()
{
    module="$1"

    pushd "${module}"

    mvn clean appengine:deploy verify \
      -Dapp.deploy.project="${gcloud_projectid}" \
      -Dapp.deploy.version="${test_runid}" \
      -Dapp.deploy.promote=false

    ret=$?

    popd

    return ${ret}
}

gcloud info
gcloud auth list

gcloud_projectid=$(gcloud info | sed -rn 's/Project: \[(.*)\]/\1/p')
test_runid=$(date +%Y%m%d-%H%M%S)

echo "GCloud Project: $gcloud_projectid"

# Push the as-built jetty9:latest to gcr.io/${gcloud_projectid}/jetty9:testing"

docker tag "jetty9:latest" "gcr.io/${gcloud_projectid}/jetty9:testing"
gcloud docker push "gcr.io/${gcloud_projectid}/jetty9:testing"

# Deploy and Execute the individual test-war-* tests

for module in test-war-*
do
  echo "module: $module"
  appengine_deploy "${module}"
done

