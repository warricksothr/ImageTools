#!/bin/bash
HOME=$PWD
cd ../..
#Update the release version of the project
#version string will look similar to this: 0.1.0-DEV-27-060aec7
VERSION=$(head -1 ./version.info)

#do work on the version to get the correct info
#we need the version string from above to look like this: 0.1.1-DEV
IFS='.' read -a arr <<< "$VERSION"
#results in [0,1,0-DEV-27-060aec7]
IFS='-' read -a arr2 <<< "${arr[2]}"
#results in [0,DEV,27,060aec7]
let major=${arr[0]}+1
#echo $major
VERSION="$major.0.0-${arr2[1]}"
#echo $VERSION

#update the POM
mvn versions:set -DnewVersion=$VERSION

cd $HOME
. createBuildRelease.sh

#commit the new patch version
git commit -a -m "Creating major version $VERSION"

#tag the build
git tag -a v$VERSION -m "Major Release Version $VERSION"

#push the build and tag
git push --follow-tags
