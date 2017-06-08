#!/bin/bash

# Copyright 2017 Google Inc. All rights reserved.

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at

#     http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Setup environment for CI with GKE.
# This include:
# - Building a test application into a docker image
# - Upload the image to Google Container Registry
# - Create a Kubernetes cluster on GKE
# - Deploy the application
# - Run the integration test
set -e

readonly dir=$(dirname $0)
readonly projectRoot="$dir/.."
readonly testAppDir="$projectRoot/tests/runtimes-common-testing"
readonly deployDir="$testAppDir/target/deploy"

readonly projectName=$(gcloud info \
                | awk '/^Project: / { print $2 }' \
                | sed 's/\[//'  \
                | sed 's/\]//')
readonly imageName="jetty-gke-integration"
readonly imageUrl="gcr.io/$projectName/$imageName"
readonly clusterName="jetty-gke-integration"

readonly imageUnderTest=$1
if [[ -z "$imageUnderTest" ]]; then
  echo "Usage: ${0} <image_under_test>"
  exit 1
fi

# Build the test app
pushd ${testAppDir}
  mvn -B clean install
popd

# Deploy to Google Container Engine
pushd ${deployDir}
export STAGING_IMAGE=${imageUnderTest}
envsubst '$STAGING_IMAGE' < Dockerfile.in > Dockerfile
export TESTED_IMAGE=${imageUrl}
envsubst < "kubernetes-configuration.yaml.in" > "kubernetes-configuration.yaml"

echo "Deploying image to Google Container Registry..."
gcloud docker -- build -t "$imageName" .
gcloud docker -- tag "$imageName" "$imageUrl"
gcloud docker -- push gcr.io/${projectName}/${imageName}

echo "Creating or searching for a Kubernetes cluster..."
TEST_CLUSTER_EXISTENCE=$(gcloud container clusters list | awk "/$clusterName/")
if [ -z "$TEST_CLUSTER_EXISTENCE" ]; then
  gcloud container clusters create "$clusterName" --num-nodes=1 --disk-size=10
fi

echo "Deploying application to Google Container Engine..."
gcloud container clusters get-credentials ${clusterName}
kubectl apply -f "kubernetes-configuration.yaml"
popd

echo "Waiting for the application to be accessible (expected time: ~1min)"
DEPLOYED_APP_URL=""
while [[ -z "$DEPLOYED_APP_URL" ]]; do
  sleep 5
  DEPLOYED_APP_URL=$(kubectl describe services jetty-runtimes-common-testing \
                             | awk '/LoadBalancer Ingress/ { print $3 }')
done

# The load balancer service may take some time to expose the application
# (~ 2 min on the cluster creation)
until $(curl --output /dev/null --silent --head --fail "http://${DEPLOYED_APP_URL}"); do
  sleep 2
done

# run in cloud container builder
echo "Running integration tests on application that is deployed at $DEPLOYED_APP_URL"
gcloud container builds submit \
  --config ${dir}/integration_test.yaml \
  --substitutions "_DEPLOYED_APP_URL=http://$DEPLOYED_APP_URL" \
  ${dir}