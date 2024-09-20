#!/bin/bash

if [ $# -eq 0 ]; then
    echo "Please provide the path to your 1024x1024 icon."
    exit 1
fi

input_icon="$1"
iconset_name="menubar"

mkdir -p "$iconset_name"

sips -z 22 22     "$input_icon" --out "${iconset_name}/upvpn_badge@1x.png"
sips -z 44 44     "$input_icon" --out "${iconset_name}/upvpn_badge@2x.png"
sips -z 66 66     "$input_icon" --out "${iconset_name}/upvpn_badge@3x.png"



echo "Menubar images created successfully."
