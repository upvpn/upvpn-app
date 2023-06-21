#!/usr/bin/env bash

set -euo pipefail

VERSION=${1:-latest}

mkdir -p ./packages

sd "APP_VERSION" "${VERSION}" ./pkg/root/Applications/upvpn.app/Contents/Info.plist
sd "APP_VERSION" "${VERSION}" ./pkg/Distribution

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
    "upvpn-${VERSION}.pkg"
