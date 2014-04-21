#!/bin/bash
echo "Welcome to Image Tools version: ${project.version}"
command="-Xmx1500m -jar ${project.name}-${project.version}.jar"
java $command
