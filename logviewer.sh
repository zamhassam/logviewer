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
EXECUTABLE="${SCRIPT_DIR}/build/libs/logviewer-1.0-SNAPSHOT-all.jar"

pushd ${SCRIPT_DIR}
./gradlew shadowJar
popd

if [[ -z ${ARGS} ]]
then
    echo "---------------------------------------------------"
    echo "Optional Arguments:"
    echo "  --fix-xml comma separated list of FIX specs"
    echo "  --log-file file to read (only use when not piping)"
    echo "---------------------------------------------------"
fi

if [ -z "${INPUT_DATA}" ]
then
    java -jar ${EXECUTABLE} -n ${ARGS}
else
    echo -e ${INPUT_DATA} | java -jar ${EXECUTABLE} ${ARGS} -n
fi