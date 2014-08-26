#!/bin/bash
#Move out of the build folder
cd ../..

packageProject ()
{
    VERSION=$1
    RELEASE=$3
    ROOT=$PWD
    cd $2

    #Getting variables that are produced by the script
    NAME=$(head -1 ./name.info)
    PACKAGENAME=$NAME-$VERSION
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
VERSION=$(head -1 ./version.info)
RELEASE="$PWD/target"

#make sure release exists
if [ ! -d $RELEASE ]; then
    mkdir $RELEASE
fi

packageProject $VERSION "./cli" $RELEASE
packageProject $VERSION "./gui" $RELEASE
#packageProject $VERSION "./daemon" $RELEASE
