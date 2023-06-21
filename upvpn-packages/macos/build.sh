#!/usr/bin/env bash

set -euo pipefail

VERSION=${1:-latest}

mkdir -p ./packages

declare -a BINARIES=(
    "./pkg/root/Applications/upvpn.app/Contents/Resources/upvpn"
    "./pkg/root/Applications/upvpn.app/Contents/Resources/upvpn-daemon"
    "./pkg/root/Applications/upvpn.app/Contents/MacOS/upvpn-ui"
)

sd "APP_VERSION" "${VERSION}" ./pkg/root/Applications/upvpn.app/Contents/Info.plist
sd "APP_VERSION" "${VERSION}" ./pkg/Distribution


if [[ ! -z "${APPLICATION_SIGNING_IDENTITY:-}" ]] && [[ ! -z "${APPLE_TEAM_ID:-}" ]]; then
    sd "APPLE_TEAM_ID" "${APPLE_TEAM_ID}" app.entitlements
    for binary in "${BINARIES[@]}"
    do
        echo "Signing: ${binary}"
        codesign \
            --entitlements app.entitlements \
            --options runtime \
            --sign "${APPLICATION_SIGNING_IDENTITY}" \
            "${binary}"
    done
fi

pkgbuild  \
    --install-location /Applications \
    --identifier app.upvpn.macos \
    --version "${VERSION}" \
    --scripts "./pkg/scripts" \
    --root "./pkg/root/Applications" \
    ./packages/app.upvpn.macos.pkg

productbuild \
    --distribution "./pkg/Distribution" \
    --resources "./pkg/Resources" \
    --package-path ./packages \
    "upvpn-${VERSION}-unsigned.pkg"

if [ ! -z "${INSTALLER_SIGNING_IDENTITY:-}" ]; then
    echo "Signing pkg"
    productsign \
        --sign "${INSTALLER_SIGNING_IDENTITY}" \
        "upvpn-${VERSION}-unsigned.pkg" "upvpn-${VERSION}.pkg"
else
    mv "upvpn-${VERSION}-unsigned.pkg" "upvpn-${VERSION}.pkg"
fi
