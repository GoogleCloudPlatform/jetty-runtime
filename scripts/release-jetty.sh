#!/bin/bash
export KOKORO_GITHUB_DIR=${KOKORO_ROOT}/src/github
source ${KOKORO_GFILE_DIR}/kokoro/common.sh

cd ${KOKORO_GITHUB_DIR}

source ./scripts/build.sh --docker-namespace ${DOCKER_NAMESPACE} --project ${GCP_TEST_PROJECT}

METADATA=$(pwd)/METADATA
cd ${KOKORO_GFILE_DIR}/kokoro
python note.py jetty -m ${METADATA} -t ${TAG}
