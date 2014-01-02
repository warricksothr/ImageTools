#Getting variables that are produced by the script
VERSION=$(head -1 ./version.info)
NAME=$(head -1 ./name.info)
PACKAGENAME=$NAME-$VERSION
PACKAGETAR=$PACKAGENAME.tar.gz
PACKAGEZIP=$PACKAGENAME.zip
TARGET=$PWD/target

cd target/build/
echo "creating $PACKAGETAR in $TARGET"
tar -zcvf $TARGET/$PACKAGETAR .
echo "creating $PACKAGEZIP in $TARGET"
zip -r $TARGET/$PACKAGEZIP ./*
