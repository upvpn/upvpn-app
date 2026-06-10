#!/usr/bin/env bash

# Copyright (C) 2023 upvpn LLC, GPL-3.0
# Based on: Copyright (C) 2022 Mullvad VPN AB, GPL-3.0

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function install_deps {
    mkdir -p ${SCRIPT_DIR}/downloads

    if [ ! -d "${SCRIPT_DIR}/downloads/Microsoft.ArtifactSigning.Client" ]; then
        cd ${SCRIPT_DIR}/downloads
        curl https://dist.nuget.org/win-x86-commandline/latest/nuget.exe -o nuget.exe
        ./nuget.exe install Microsoft.ArtifactSigning.Client -x
        cd -
    else
        echo "Dependencies present"
    fi
}

# Sign all binaries passed as arguments to this function
function sign_win {
    local NUM_RETRIES=3

    for binary in "$@"; do
        # Try multiple times in case the timestamp server cannot
        # be contacted.
        for i in $(seq 0 ${NUM_RETRIES}); do
            echo "Signing $binary..."
            if signtool sign \
                -debug \
                -tr http://timestamp.acs.microsoft.com \
                -td sha256 \
                -fd sha256 \
                -d "UpVPN app" \
                -dlib ${SCRIPT_DIR}/downloads/Microsoft.ArtifactSigning.Client/bin/x64/Azure.CodeSigning.Dlib.dll \
                -dmdf "${SCRIPT_DIR}/metadata.json" \
                -du "https://github.com/upvpn/upvpn-app#readme" \
                "$binary"
            then
                break
            fi

            if [ "$i" -eq "${NUM_RETRIES}" ]; then
                return 1
            fi

            sleep 1
        done
    done
    return 0
}

if [[ "$SIGN" == "true" && "$(uname -s)" == "MINGW"* ]]; then
    install_deps
    sign_win "$@"
fi
