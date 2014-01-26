Development Information
=============

Requirements
------------

Java 7 JDK (Java 8 is not currently supported due to incompatabilities between it and certain libraries)
Maven 3.0+

NOTE:
The Java 7 JDK on arm platforms are missing required javafx jar files. These files can be copied over from an x86 distribution and the project will build and test, however the binaries will still be missing, so the application will not run in GUI mode. Commandline or deamon mode should be absolutely fine however.

The following files must be copied for an ARM system to compile and test the source
(x86/x86_64 JDK folder)/lib/ant-javafx.jar -> $JAVA_HOME/lib/ant-javafx.jar
(x86/x86_64 JDK folder)/jre/lib/jfxrt.jar -> $JAVA_HOME/jre/lib/jfxrt.jar

Getting Started
------------

If this is the first time compiling the project do the following
(on nix* systems) run the included fixJavaFXClasspath.sh script
OR
(on windows) run "mvn com.zenjava:javafx-maven-plugin:2.0:fix-classpath" from the commandline in the source directory

("clean" can be removed from the follwing commands if desired)
To compile the project run "mvn clean compile"
To test the project run "mvn clean test"
To package the project for distribution run "mvn clean jfx:jar". (The contents of the ./target/jfx/app directory may then be packaged into a zip for distribution)

Debugging and Configuration
------------

The app is configured from two files (three if you have already ran it once)
For deployment versions the default configuration can be found in ./src/main/resources/default.properties
For test versions the deafult configuration can be found in ./src/test/resources/default.properties
For all versions a user.properties file is generated on first run and it will override the values in the default.properties
For all versions an example log4j.properties file is included and will be read at runtime. If it does not exist, the logging will be configured internally. For the internal logging there are properties in the default file that control the level of logging. debug will write to Image-Tools.debug, info to Image-Tools.info, and error to Image-Tools.err. All of these are rolling appenders with a few hundred k file limit and a maximum of 1 previous log. Feel free to include or use your own log4j.properties file to define loggers that suit your needs.

Development Methodology
------------

Image Tools should be treated as a test driven application for all core features. Ideally before commiting to the main branch, all tests should run succesfull. Additionally in the interest of system compatability (be it either a nix*, OSX, or windows system) no test should use system specific features.

Currently the application exists in one super jar with dependancies in an accompanying lib folder. This is not viewed as ideal and a goal in the future will be to split the engine and its functionalities from the GUI, CLI, and Daemon interfaces so that seperate artifacts may be generated for each. This will resolve the current issues of building the GUI on ARM based systems.

Image Tools is a mixed language project. Java provides the interface code and support (currently JavaFX) and Scala provides the grunt work. Because of this design we are locked to the latest JDK that the dependent scala libraries are built against. (currently Java 1.7) When support for new versions or Java become available a branch will be spun off to test compatabilites and test cases. If everything passes, that upgrade can then be merged back into trunk.

Limited samples should be included in version control and ideally none should be included as they massively increase the repository size. Instead all samples required to run the tests should be provided as a seperate package to be downloaded by the developer for inclusion before running the tests.

Image Tools uses the Semantic Versioning scheme to represent changes within the code base. The initial version committed was v0.1.0-DEV. Tagged versions should follow this scheme. In addition to Semantic Versioning the follwing tags are used. DEV (indicates a development version, very unstable, may not pass all tests), SNAPSHOT (unstable versions, passes all tests but not all features may be ready for release), M{number} i.e. M1 (sequential milestone release for a specific version number. This build is 'expected' to be stable, but may not be completely stable. All tests pass and are consistent on known platforms.), FINAL (last supported version of a major.minor pair. No more updates will be released along this line and upgrading to a newer major.minor pair is advised. Typically used when deprecated functionality is cut off.) If no tag is included it is safe to assume it is a production version and has made it out of milestone , but has not yet reached FINAL so more revision changes may occur as bugs are fixed.

