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
let minor=${arr[1]}+1
#echo $minor
VERSION="${arr[0]}.$minor.0-${arr2[1]}"
#echo $VERSION

#update the POM
mvn versions:set -DnewVersion=$VERSION

#commit the new patch version
git commit -a . -m "Creating minor version $VERSION"

#tag the build
git tag -a v$VERSION -m "Minor Release Version $VERSION"

#push the build and tag
git push --follow-tags

. build.sh
. package.sh
