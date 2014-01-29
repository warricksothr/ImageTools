#!/bin/bash
echo "Welcome to Image Tools version: ${project.version}"
command=""
correct=false
while true; do
	read -p "Please enter and commandline arguments you would like to include: " args
	command="-Xmx1.5G -cp ${project.name}-${project.version}-jfx.jar:lib/* com.sothr.imagetools.AppCLI $args"
	echo "Is \"$command\" accurate? (yes/no)"
	select yn in "Yes" "No"; do
		case $yn in
			Yes ) correct=true; break;;
			No ) break;;
		esac
	done
	case $correct in
		true ) break;;
	esac
done	
java $command
