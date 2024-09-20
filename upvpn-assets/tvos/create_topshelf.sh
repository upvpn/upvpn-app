#!/bin/bash

if [ $# -eq 0 ]; then
    echo "Please provide the path to your 4640x1440 image"
    exit 1
fi

input_icon="$1"


sips -z 720 2320 "$input_icon" --out "top_shelf2320x720.png"


echo "created successfully."
