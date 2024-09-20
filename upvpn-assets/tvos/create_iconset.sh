#!/bin/bash

if [ $# -eq 0 ]; then
    echo "Please provide the path to your 1280x768 icon."
    exit 1
fi

input_icon="$1"
name=${2:-"iconTVOS"}


sips -z 240 400 "$input_icon" --out "${name}_400x240@1x.png"
sips -z 480 800 "$input_icon" --out "${name}_800x480@2x.png"


echo "Icons created successfully."
