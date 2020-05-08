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

#
# Fetch and execute the structure test framework run script.
set -e
set -x
dir=`dirname $0`
#scriptPath=https://storage.googleapis.com/container-structure-test/v1.1.0/container-structure-test
#scriptPath=https://storage.googleapis.com/container-structure-test/v1.2.0/container-structure-test-darwin-amd64
scriptPath=$6

destDir=$4
fileName=$destDir/container-structure-test

if [ ! -d $destDir ]
then
  mkdir -p $destDir
fi

if [[ ! -f $fileName ]]
then
    wget -O $fileName --no-verbose $scriptPath
fi

chmod +x $fileName

IMAGE=$1
WORKSPACE=$2
CONFIG=$3
TEST_IMAGE="${IMAGE}-struct-test"

pushd $5
echo "Creating temporary image $TEST_IMAGE"
pwd
docker build --build-arg=RUNTIME_IMAGE=$IMAGE -f Dockerfile -t $TEST_IMAGE workspace
popd

$fileName test --image $TEST_IMAGE --config $CONFIG
