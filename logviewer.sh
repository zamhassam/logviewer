#!/usr/bin/env bash
set -euo pipefail
INPUT_DATA=""

if [[ ! -t 0 ]]
then
    while read input
    do
        INPUT_DATA="${INPUT_DATA}\n${input}"
    done
fi
SCRIPT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
ARGS="${@}"
EXECUTABLE="${SCRIPT_DIR}/build/install/logviewer/bin/logviewer"

pushd ${SCRIPT_DIR}
./gradlew installDist
popd

if [[ -z ${ARGS} ]]
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
    echo -e ${INPUT_DATA} | bash ${EXECUTABLE} ${ARGS} -n
fi