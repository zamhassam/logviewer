#!/usr/bin/env bash
set -euo pipefail
INPUT_DATA=""

if [ ! -t 0 ]
then
    read INPUT_DATA
fi

./gradlew  installDist

SCRIPT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
ARGS="${@}"
EXECUTABLE="${SCRIPT_DIR}/build/install/logviewer/bin/logviewer"

if [ -z ${ARGS} ]
then
    echo "---------------------------------------------------"
    echo "Optional Arguments:"
    echo "  --fix-xml space separated list of FIX specs"
    echo "  --log-file file to read (only use when not piping)"
    echo "---------------------------------------------------"
fi

if [ -z "${INPUT_DATA}" ]
then
    bash ${EXECUTABLE} ${ARGS}
else
    echo ${INPUT_DATA} | bash ${EXECUTABLE} ${ARGS} -n
fi