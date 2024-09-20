#!/bin/bash

if [ $# -eq 0 ]; then
    echo "Please provide the path to your 1024x1024 icon."
    exit 1
fi

input_icon="$1"
iconset_name="AppIcon.iconset"

mkdir -p "$iconset_name"

sips -z 16 16     "$input_icon" --out "${iconset_name}/icon_16x16.png"
sips -z 32 32     "$input_icon" --out "${iconset_name}/icon_16x16@2x.png"
sips -z 32 32     "$input_icon" --out "${iconset_name}/icon_32x32.png"
sips -z 64 64     "$input_icon" --out "${iconset_name}/icon_32x32@2x.png"
sips -z 128 128   "$input_icon" --out "${iconset_name}/icon_128x128.png"
sips -z 256 256   "$input_icon" --out "${iconset_name}/icon_128x128@2x.png"
sips -z 384 384   "$input_icon" --out "${iconset_name}/icon_128x128@3x.png"
sips -z 256 256   "$input_icon" --out "${iconset_name}/icon_256x256.png"
sips -z 512 512   "$input_icon" --out "${iconset_name}/icon_256x256@2x.png"
sips -z 512 512   "$input_icon" --out "${iconset_name}/icon_512x512.png"
cp "$input_icon" "${iconset_name}/icon_512x512@2x.png"

iconutil -c icns "$iconset_name"

#rm -R "$iconset_name"

echo "Icon set created successfully."
