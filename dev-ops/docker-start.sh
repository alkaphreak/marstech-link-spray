#!/bin/bash

# Vérifier si GitHub Desktop est en cours d'exécution
if ! pgrep -x "Docker\ Desktop" > /dev/null
then
    echo "Lancement de GitHub Desktop..."
    open -a Docker;
    sleep 5;
else
    echo "GitHub Desktop est déjà en cours d'exécution."
fi
exit 0
