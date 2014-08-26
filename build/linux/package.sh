#!/bin/bash
#Move out of the build folder
cd ../..

packageProject ()
{
    local LOCAL_VERSION=$1
    RELEASE=$3
    ROOT=$PWD
    cd $2

    #Getting variables that are produced by the script
    NAME=$(head -1 ./name.info)
    PACKAGENAME=$NAME-$LOCAL_VERSION
    PACKAGETAR=$PACKAGENAME.tar.gz
    PACKAGEZIP=$PACKAGENAME.zip
    TARGET=$PWD/target

    #Packaging Jar Distributable
    cd target/release/
    echo "creating $PACKAGETAR in $TARGET"
    tar -zcvf $RELEASE/$PACKAGETAR .
    echo "creating $PACKAGEZIP in $TARGET"
    zip -r $RELEASE/$PACKAGEZIP ./*
    #Removing prebuilt jar in target
    rm $TARGET/*.jar
    #Copying Jar to target for archiving
    cp ./*.jar $TARGET

    cd $ROOT
}

#Getting variables that are produced by the script
VERSIONSTRING=$(head -1 ./version.info)
RELEASE="$PWD/target"

#make sure release exists and is empty
if [ -d $RELEASE ]; then
    rm -R $RELEASE
fi
mkdir $RELEASE

packageProject $VERSIONSTRING "./cli" $RELEASE
packageProject $VERSIONSTRING "./gui" $RELEASE
#packageProject $VERSIONSTRING "./daemon" $RELEASE
