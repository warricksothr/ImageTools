#!/bin/bash
HOME=$PWD
#Do not change versions as this is a build release with artifacts
#Build and package the current version
. build.sh
cd $HOME
. package.sh
