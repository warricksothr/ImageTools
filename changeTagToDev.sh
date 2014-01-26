#!/bin/bash
#Update the release version of the project
#version string will look similar to this: 0.1.0-DEV-27-060aec7
VERSION=$(head -1 ./version.info)

#do work on the version to get the correct info
#we need the version string from above to look like this: 0.1.1-DEV
IFS='.' read -a arr <<< "$VERSION"
#results in [0,1,0-DEV-27-060aec7]
IFS='-' read -a arr2 <<< "${arr[2]}"
#results in [0,DEV,27,060aec7]
VERSION="${arr[0]}.${arr[1]}.${arr2[0]}-DEV"
#echo $VERSION

#update the POM
mvn versions:set -DnewVersion=$VERSION

echo "$VERSION" > version.info