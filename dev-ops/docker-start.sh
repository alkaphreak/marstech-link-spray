#!/bin/bash
#
# Check if Docker Desktop is running
if [[ "$(uname)" == "Darwin" ]]
then
    if ! pgrep -x "Docker\ Desktop" > /dev/null
    then
        echo "Starting Docker Desktop..."
        open -a Docker;
        sleep 5;
    else
    echo "Docker Desktop is already running."
    fi
fi
exit 0
