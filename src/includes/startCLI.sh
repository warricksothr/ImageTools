#!/bin/bash
echo "Welcome to Image Tools version: ${project.version}"
args=""
while (( "$#" )); do 
  args="$args $1" 
  shift 
done
command="-Xmx1500m -cp ${project.name}-${project.version}-jfx.jar:lib/* com.sothr.imagetools.AppCLI"
correct=false
#Check for existing commands and use them instead of asking if possible
if [[ -z "$args" ]]
then
    while true; do
    	read -p "Please enter and commandline arguments you would like to include: " args
    	command="$command $args"
    	echo "Is \"$command\" accurate? (yes/no)"
    	select yn in "Yes" "No"; do
    		case $yn in
    			Yes ) correct=true; java $command; exit;;
    			No ) break;;
    		esac
    	done
    	case $correct in
    		true ) break;;
    	esac
    done   
else
    command="$command $args"
    echo "Is \"$command\" accurate? (yes/no)"
	select yn in "Yes" "No"; do
		case $yn in
			Yes ) java $command; exit;;
			No ) exit;;
		esac
	done
fi