#!/bin/bash
# This script updates the version of a Maven project to the next version.
# It is assumed that the script is run from the root directory of the project.
VERSION="0.0.4-SNAPSHOT"
mvn versions:set -DnewVersion=$VERSION
