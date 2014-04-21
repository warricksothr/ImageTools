#!/bin/bash
#Getting variables that are produced by the script
VERSION=$(head -1 ./version.info)
NAME=$(head -1 ./name.info)
PACKAGENAME=$NAME-$VERSION
PACKAGETAR=$PACKAGENAME.tar.gz
PACKAGEZIP=$PACKAGENAME.zip
TARGET=$PWD/target

#Packaging Jar Distributable
cd target/release/
echo "creating $PACKAGETAR in $TARGET"
tar -zcvf $TARGET/$PACKAGETAR .
echo "creating $PACKAGEZIP in $TARGET"
zip -r $TARGET/$PACKAGEZIP ./*
#Removing prebuilt jar in target
rm $TARGET/*.jar
#Copying Jar to target for archiving
cp ./*.jar $TARGET
