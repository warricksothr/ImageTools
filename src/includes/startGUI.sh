#!/bin/bash
echo "Welcome to Image Tools version: ${project.version}"
command="-Xmx1.5G -jar ${project.name}-${project.version}-jfx.jar"
java $command