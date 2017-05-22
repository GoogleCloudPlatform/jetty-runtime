#!/bin/bash
# Adds bits of configuration necessary for the Cloud Debugger to work with the Jetty image.
# It needs to run before 20-debug-env.bash from the openjdk image.

# Set CDBG_APP_WEB_INF_DIR, used by CDBG in format-env-appengine-vm.sh
export CDBG_APP_WEB_INF_DIR="${JETTY_BASE}/webapps/root/WEB-INF"