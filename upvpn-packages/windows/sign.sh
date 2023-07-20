#!/usr/bin/env bash

# Copyright (C) 2023 upvpn LLC, GPL-3.0
# Based on: Copyright (C) 2022 Mullvad VPN AB, GPL-3.0

# Sign all binaries passed as arguments to this function
function sign_win {
    local NUM_RETRIES=3

    for binary in "$@"; do
        # Try multiple times in case the timestamp server cannot
        # be contacted.
        for i in $(seq 0 ${NUM_RETRIES}); do
            echo "Signing $binary..."
            if signtool sign \
                -tr http://timestamp.sectigo.com -td sha256 \
                -fd sha256 -d "upvpn app" \
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
    sign_win "$@"
fi
