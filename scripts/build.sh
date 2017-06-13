#!/bin/bash

# Copyright 2016 Google Inc. All rights reserved.

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at

#     http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -e

usage() {
  echo "Usage: ${0} -d <docker_namespace> [-t <docker_tag>] [-p <gcp_test_project>]"
  exit 1
}

# Parse arguments to this script
while [[ $# -gt 1 ]]; do
  key="$1"
  case $key in
    -d|--docker-namespace)
    DOCKER_NAMESPACE="$2"
    shift
    ;;
    -t|--tag)
    DOCKER_TAG="$2"
    shift # past argument
    ;;
    -p|--project)
    GCP_TEST_PROJECT="$2"
    shift # past argument
    ;;
    *)
    # unknown option
    usage
    ;;
  esac
  shift
done

dir=$(dirname $0)
projectRoot=${dir}/..
buildConfigDir=${projectRoot}/build/config

RUNTIME_NAME="jetty"
DOCKER_TAG_PREFIX="9.4"

if [ -z "${DOCKER_NAMESPACE}" ]; then
  usage
fi

BUILD_TIMESTAMP="$(date -u +%Y-%m-%d_%H_%M)"
if [ -z "${DOCKER_TAG}" ]; then
  DOCKER_TAG="${DOCKER_TAG_PREFIX}-${BUILD_TIMESTAMP}"
fi

if [ -z "${GCP_TEST_PROJECT}" ]; then
  GCP_TEST_PROJECT="$(gcloud config list --format='value(core.project)')"
fi

IMAGE="${DOCKER_NAMESPACE}/${RUNTIME_NAME}:${DOCKER_TAG}"
echo "IMAGE: $IMAGE"

STAGING_IMAGE="gcr.io/${GCP_TEST_PROJECT}/${RUNTIME_NAME}_staging:${DOCKER_TAG}"
AE_SERVICE_BASE="$(echo $BUILD_TIMESTAMP | sed 's/_//g')"
TEST_AE_SERVICE_1="${AE_SERVICE_BASE}-v1"
TEST_AE_SERVICE_2="${AE_SERVICE_BASE}-v2"

set +e
set -x
gcloud container builds submit \
  --config=${buildConfigDir}/build.yaml \
  --substitutions=\
"_IMAGE=$IMAGE,"\
"_DOCKER_TAG=$DOCKER_TAG,"\
"_STAGING_IMAGE=$STAGING_IMAGE,"\
"_GCP_TEST_PROJECT=$GCP_TEST_PROJECT,"\
"_TEST_AE_SERVICE_1=$TEST_AE_SERVICE_1,"\
"_TEST_AE_SERVICE_2=$TEST_AE_SERVICE_2"\
  --timeout=20m \
  $projectRoot

testResult=$?

# once build has completed, kick off async cleanup build
gcloud container builds submit \
  --config=${buildConfigDir}/cleanup.yaml \
  --substitutions=\
"_GCP_TEST_PROJECT=$GCP_TEST_PROJECT,"\
"_TEST_AE_SERVICE_1=$TEST_AE_SERVICE_1,"\
"_TEST_AE_SERVICE_2=$TEST_AE_SERVICE_2"\
  --async \
  --no-source

exit $testResult
